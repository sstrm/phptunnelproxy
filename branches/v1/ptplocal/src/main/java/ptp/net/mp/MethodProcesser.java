package ptp.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.util.Base64Coder;
import ptp.util.ByteArrayUtil;

public abstract class MethodProcesser {
	private static Logger log = Logger.getLogger(MethodProcesser.class);
	protected static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	protected HttpHead reqHH;

	public static MethodProcesser getIns(InputStream inFromBrowser,
			OutputStream outToBrowser) throws ProxyException {
		MethodProcesser mp = null;

		HttpHead reqHH = new HttpHead(inFromBrowser, (byte) 0);

		if (reqHH.getMethodName().equals("GET"))
			mp = new GetMethodProcesser(inFromBrowser, outToBrowser);
		else if (reqHH.getMethodName().equals("POST"))
			mp = new PostMethodProcesser(inFromBrowser, outToBrowser);
		else {
			throw new ProxyException("Not supportted http Method: "
					+ reqHH.getMethodName());
		}

		mp.reqHH = reqHH;

		log.info(mp.reqHH.getMethodName() + ": " + mp.reqHH.getDestURL());
		return mp;
	}

	void requestRemote(byte[] data, String destHost, int destPort,
			boolean isSSL, OutputStream outToBrowser) throws ProxyException {
		
		log.debug("request data: \n"
				+ ByteArrayUtil.toString(data, 0, data.length));

		URL remotePhpURL = Config.getIns().getRemotePhpURL();
		log.info("remotePhp: " + remotePhpURL.toString());

		String requestBase64String = new String(Base64Coder.encode(data, 0,
				data.length));
		String destHostBase64String = Base64Coder.encodeString(destHost);

		String requestEncodedString = null;
		String destHostEncodedString = null;

		try {
			requestEncodedString = URLEncoder.encode(requestBase64String,
					"US-ASCII");
			destHostEncodedString = URLEncoder.encode(destHostBase64String,
					"US-ASCII");
		} catch (UnsupportedEncodingException e2) {
		}

		byte key = (byte) ((Math.random() * (64)) + 1);

		byte[] postData = null;

		postData = ByteArrayUtil
				.getBytesFromString("request_data=" + requestEncodedString
						+ "&dest_host=" + destHostEncodedString + "&dest_port="
						+ destPort + "&is_ssl=" + isSSL + "&key=" + key);

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
			inFromPhp = remotePhpConn.getInputStream();
		} catch (IOException e) {
			throw new ProxyException(e);
		}

		HttpHead resHH = new HttpHead(inFromPhp, key);

		resHH.removeHeader("Keep-Alive");
		resHH.removeHeader("Connection");
		resHH.setHeader("Proxy-Connection", "keep-alive");
		resHH.setHeader("X-PTP-User-Agent", Config.getIns().getUserAgent());
		resHH.setHeader("X-PTP-Thread-Name", Thread.currentThread().getName());
		resHH.setHeader("X-PTP-Remote-PHP", remotePhpURL.toString());
		resHH.setHeader("X-PTP-Key", String.valueOf(key));

		try {
			outToBrowser.write(resHH.getHeadBytes());
			outToBrowser.flush();

			int readCount = -1;
			int phpByteSize = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.buff.size", "102400"));
			byte[] phpByte = new byte[phpByteSize];

			while ((readCount = inFromPhp.read(phpByte, 0, phpByteSize)) != -1) {
				ByteArrayUtil.decrypt(phpByte, 0, readCount, key);
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

	public abstract void process() throws ProxyException;

}
