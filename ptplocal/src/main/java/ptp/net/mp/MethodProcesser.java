package ptp.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.net.util.HttpUtil;
import ptp.util.Base64Coder;
import ptp.util.ByteArrayUtil;
import ptp.util.URLUtil;

public abstract class MethodProcesser {
	private static Logger log = Logger.getLogger(MethodProcesser.class);
	protected static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	protected String reqLine;
	protected Map<String, String> reqHeaders = new HashMap<String, String>();

	protected String destHost;
	protected int destPort;

	public static MethodProcesser getIns(InputStream inFromBrowser,
			OutputStream outToBrowser) throws ProxyException {
		MethodProcesser mp = null;

		byte[] buff = new byte[buff_size];
		int headLength = 0;
		headLength = HttpUtil.readHttpHead(buff, inFromBrowser, (byte) 0);

		String headString = ByteArrayUtil.toString(buff, 0, headLength);
		String[] headArray = headString.split("\\r\\n");

		if (headArray[0].startsWith("GET"))
			mp = new GetMethodProcesser(inFromBrowser, outToBrowser);
		else if (headArray[0].startsWith("POST"))
			mp = new PostMethodProcesser(inFromBrowser, outToBrowser);
		else {
			throw new ProxyException("Not supportted http Method: " + headArray[0]);
		}

		mp.reqLine = headArray[0];

		for (int i = 1; i < headArray.length; i++) {
			String[] tokens = headArray[i].split(":\\s");

			if (tokens.length == 2) {
				mp.reqHeaders.put(tokens[0], tokens[1]);
			} else if (tokens.length == 1) {
				mp.reqHeaders.put(tokens[0], "");
			} else {
				// what a fucking header?!
				throw new ProxyException("Get a wrong http header: "
						+ headArray[i]);
			}
		}

		return mp;
	}

	protected void readHttpHeaders(InputStream in, byte key)
			throws ProxyException {

	}

	void requestRemote(byte[] data, String destHost, int destPort,
			boolean isSSL, OutputStream outToBrowser) throws ProxyException {
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
		try {

			int resHeadLength = -1;
			byte[] resHeadBuff = new byte[buff_size];

			resHeadLength = HttpUtil.readHttpHead(resHeadBuff, inFromPhp, key);
			String responseHead = ByteArrayUtil.toString(resHeadBuff, 0,
					resHeadLength);
			String[] resHeadArray = responseHead.split("\\r\\n");
			String resLine = resHeadArray[0];
			Map<String, String> resHeaders = new HashMap<String, String>();
			for (int i = 0; i < resHeadArray.length; i++) {
				String[] tokens = resHeadArray[i].split(":\\s");

				if (tokens.length == 2) {
					resHeaders.put(tokens[0], tokens[1]);
				} else if (tokens.length == 1) {
					resHeaders.put(tokens[0], "");
				} else {
					// what a fucking header?!
					throw new ProxyException("Get a wrong http header: "
							+ resHeadArray[i]);
				}
			}

			resHeaders.remove("Proxy-Connection");
			resHeaders.remove("Keep-Alive");
			resHeaders.put("Connection", "close");
			resHeaders.put("Proxy-Connection", "keep-alive");
			resHeaders.put("X-PTP-User-Agent", Config.getIns().getUserAgent());
			resHeaders.put("X-PTP-Thread-Name", Thread.currentThread()
					.getName());
			resHeaders.put("X-PTP-Remote-PHP", remotePhpURL.toString());
			resHeaders.put("X-PTP-Key", String.valueOf(key));

			outToBrowser.write(HttpUtil.getHeadBytes(resLine, resHeaders));
			outToBrowser.flush();

			int readCount = -1;
			int phpByteSize = Integer.parseInt(Config.getIns().getValue(
					"ptp.buff.size", "1024"));
			byte[] phpByte = new byte[phpByteSize];

			while ((readCount = inFromPhp.read(phpByte, 0, phpByteSize)) != -1) {
				ByteArrayUtil.decrypt(phpByte, 0, readCount, key);
				outToBrowser.write(phpByte, 0, readCount);
				outToBrowser.flush();
				log.debug(ByteArrayUtil.toString(phpByte, 0, readCount));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			inFromPhp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void process() throws ProxyException {
		URL requestURL = null;
		try {
			requestURL = new URL(reqLine.split("\\s")[1]);
		} catch (MalformedURLException e1) {
			// TODO
		}

		destHost = requestURL.getHost();
		log.info("destHost: " + destHost);
		destHost = Config.getIns().getIp(destHost);

		destPort = requestURL.getPort() != -1 ? requestURL.getPort() : 80;
		log.info("destPort: " + destPort);

		String method = reqLine.split("\\s")[0];
		String resourc = URLUtil.getResource(reqLine.split("\\s")[1]);
		log.info("destPath: " + resourc);
		String version = reqLine.split("\\s")[2];
		reqLine = method + " " + resourc + " " + version;

		reqHeaders.remove("Proxy-Connection");
		reqHeaders.remove("Keep-Alive");

		reqHeaders.put("Connection", "close");
	}

}
