package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.encrypt.DencryptWraperInputStream;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpHead;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpReqLine;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpResLine;
import cc.co.phptunnelproxy.ptplocal.util.Base64Coder;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;

public abstract class MethodProcesser {
	private static Logger log = Logger.getLogger(MethodProcesser.class);
	protected static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	protected HttpReqLine reqLine;
	protected HttpHead reqHH;

	public static MethodProcesser getIns(InputStream inFromBrowser,
			OutputStream outToBrowser) throws ProxyException {
		MethodProcesser mp = null;

		HttpReqLine reqLine = null;
		HttpHead reqHH = null;
		try {
			reqLine = new HttpReqLine(inFromBrowser);
			reqHH = new HttpHead(inFromBrowser);
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}

		if (reqLine.getMethodName().equals("GET"))
			mp = new GetMethodProcesser(inFromBrowser, outToBrowser);
		else if (reqLine.getMethodName().equals("POST"))
			mp = new PostMethodProcesser(inFromBrowser, outToBrowser);
		else if (reqLine.getMethodName().equals("CONNECT"))
			mp = new ConnectMethodprocesser(inFromBrowser, outToBrowser);
		else {
			throw new ProxyException("Not supportted http Method: "
					+ reqLine.getMethodName());
		}

		mp.reqLine = reqLine;
		mp.reqHH = reqHH;
		log.info(reqLine.toString());
		return mp;
	}

	public static MethodProcesser getSSLIns(InputStream inFromBrowser,
			OutputStream outToBrowser, String destHost, int destPort)
			throws ProxyException {
		MethodProcesser mp = null;

		HttpReqLine reqLine = null;
		HttpHead reqHH = null;
		try {
			reqLine = new HttpReqLine(inFromBrowser);
			reqHH = new HttpHead(inFromBrowser);
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}

		if (reqLine.getMethodName().equals("GET"))
			mp = new SSLGetMethodProcesser(inFromBrowser, outToBrowser,
					destHost, destPort);
		else if (reqLine.getMethodName().equals("POST"))
			mp = new SSLPostMethodProcesser(inFromBrowser, outToBrowser,
					destHost, destPort);
		else {
			throw new ProxyException("Not supportted http Method: "
					+ reqLine.getMethodName());
		}

		mp.reqLine = reqLine;
		mp.reqHH = reqHH;
		log.info(reqLine.toString());
		return mp;
	}

	protected void requestRemote(byte[] data, String destHost, int destPort,
			boolean isSSL, OutputStream outToBrowser) throws ProxyException {

		log.debug("request data: \n"
				+ ByteArrayUtil.toString(data, 0, data.length));

		URL remotePhpURL = Config.getIns().getRemotePhpURL();
		log.info("remotePhp: " + remotePhpURL.toString());

		int key = (int) ((Math.random() * (64)) + 1);

		byte[] postData = ByteArrayUtil.getBytesFromString("request_data="
				+ base64urlEncode(data) + "&dest_host="
				+ base64urlEncode(destHost) + "&dest_port=" + destPort
				+ "&is_ssl=" + isSSL + "&key=" + key);

		log.debug("request: "
				+ ByteArrayUtil.toString(postData, 0, postData.length));

		HttpURLConnection remotePhpConn = null;
		try {
			remotePhpConn = (HttpURLConnection) remotePhpURL
					.openConnection(Config.getIns().getProxy());
		} catch (IOException e) {
			throw new ProxyException(e);
		}
		try {
			remotePhpConn.setRequestMethod("POST");
		} catch (ProtocolException e) {

		}
		remotePhpConn.setRequestProperty("User-Agent", Config.getIns()
				.getUserAgent());
		remotePhpConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		remotePhpConn.setRequestProperty("Connection", "close");
		remotePhpConn.setUseCaches(false);
		remotePhpConn.setDoOutput(true);

		try {
			OutputStream outToPhp = remotePhpConn.getOutputStream();

			outToPhp.write(postData);

			outToPhp.flush();
			outToPhp.close();
		} catch (IOException e) {
			throw new ProxyException(e);
		}

		InputStream inFromPhp = null;
		try {
			inFromPhp = new DencryptWraperInputStream(
					remotePhpConn.getInputStream(), key);
		} catch (IOException e) {
			throw new ProxyException(e);
		}

		HttpResLine resLine = null;
		HttpHead resHH = null;
		try {
			resLine = new HttpResLine(inFromPhp);
			resHH = new HttpHead(inFromPhp);
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}

		resHH.removeHeader("Keep-Alive");
		resHH.removeHeader("Connection");
		resHH.setHeader("Proxy-Connection", "keep-alive");
		resHH.setHeader("X-PTP-User-Agent", Config.getIns().getUserAgent());
		resHH.setHeader("X-PTP-Thread-Name", Thread.currentThread().getName());
		resHH.setHeader("X-PTP-Remote-PHP", remotePhpURL.toString());
		resHH.setHeader("X-PTP-Key", String.valueOf(key));

		try {
			outToBrowser.write(resLine.getBytes());
			outToBrowser.write(resHH.getBytes());
			outToBrowser.flush();

			int readCount = -1;
			byte[] phpByte = new byte[buff_size];

			while ((readCount = inFromPhp.read(phpByte, 0, buff_size)) != -1) {
				outToBrowser.write(phpByte, 0, readCount);
				outToBrowser.flush();
				log.debug(ByteArrayUtil.toString(phpByte, 0, readCount));
			}

		} catch (SocketException se) {
			log.info("browser sotpped connection");
			// do not throw ProxyException
		} catch (IOException e) {
			throw new ProxyException(e);
		} finally {
			try {
				inFromPhp.close();
				remotePhpConn.disconnect();
			} catch (IOException e) {
				throw new ProxyException(e);
			}
		}

	}

	protected void request(URL destUrl, byte[] headData, byte[] bodyData,
			OutputStream outToBrowser) throws ProxyException {
		URL remotePhpURL = null;
		try {
			remotePhpURL = new URL("http://s1.phptunnelproxy.co.cc/ptpremote/curlremote.php");
		} catch (MalformedURLException e1) {
		}
		log.info("remotePhp: " + remotePhpURL.toString());

		int key = (int) ((Math.random() * (64)) + 1);

		byte[] postData = ByteArrayUtil.getBytesFromString("head_data="
				+ base64urlEncode(headData) + "&body_data="
				+ base64urlEncode(bodyData) + "&dest_url="
				+ base64urlEncode(destUrl.toString()) + "&key=" + key);

		log.info("request: "
				+ ByteArrayUtil.toString(postData, 0, postData.length));

		HttpURLConnection remotePhpConn = null;
		try {
			remotePhpConn = (HttpURLConnection) remotePhpURL
					.openConnection(Config.getIns().getProxy());
		} catch (IOException e) {
			throw new ProxyException(e);
		}
		try {
			remotePhpConn.setRequestMethod("POST");
		} catch (ProtocolException e) {

		}
		remotePhpConn.setRequestProperty("User-Agent", Config.getIns()
				.getUserAgent());
		remotePhpConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		remotePhpConn.setRequestProperty("Connection", "close");
		remotePhpConn.setUseCaches(false);
		remotePhpConn.setDoOutput(true);

		try {
			OutputStream outToPhp = remotePhpConn.getOutputStream();

			outToPhp.write(postData);

			outToPhp.flush();
			outToPhp.close();
		} catch (IOException e) {
			throw new ProxyException(e);
		}

		InputStream inFromPhp = null;
		try {
			inFromPhp = new DencryptWraperInputStream(
					remotePhpConn.getInputStream(), key);
		} catch (IOException e) {
			throw new ProxyException(e);
		}

		HttpResLine resLine = null;
		HttpHead resHH = null;
		try {
			resLine = new HttpResLine(inFromPhp);
			resHH = new HttpHead(inFromPhp);
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}

		resHH.removeHeader("Keep-Alive");
		resHH.removeHeader("Connection");
		resHH.setHeader("Proxy-Connection", "keep-alive");
		resHH.setHeader("X-PTP-User-Agent", Config.getIns().getUserAgent());
		resHH.setHeader("X-PTP-Thread-Name", Thread.currentThread().getName());
		resHH.setHeader("X-PTP-Remote-PHP", remotePhpURL.toString());
		resHH.setHeader("X-PTP-Key", String.valueOf(key));

		try {
			outToBrowser.write(resLine.getBytes());
			outToBrowser.write(resHH.getBytes());
			outToBrowser.flush();

			int readCount = -1;
			byte[] phpByte = new byte[buff_size];

			while ((readCount = inFromPhp.read(phpByte, 0, buff_size)) != -1) {
				outToBrowser.write(phpByte, 0, readCount);
				outToBrowser.flush();
				log.debug(ByteArrayUtil.toString(phpByte, 0, readCount));
			}

		} catch (SocketException se) {
			log.info("browser sotpped connection");
			// do not throw ProxyException
		} catch (IOException e) {
			throw new ProxyException(e);
		} finally {
			try {
				inFromPhp.close();
				remotePhpConn.disconnect();
			} catch (IOException e) {
				throw new ProxyException(e);
			}
		}
	}

	private String base64urlEncode(String src) {
		return base64urlEncode(ByteArrayUtil.getBytesFromString(src));
	}

	private String base64urlEncode(byte[] src) {
		String base64String = new String(Base64Coder.encode(src, 0, src.length));
		String urlEncodedString = null;
		try {
			urlEncodedString = URLEncoder.encode(base64String, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return urlEncodedString;
	}

	public abstract void process() throws ProxyException;

}
