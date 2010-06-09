package ptp.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.Base64Coder;
import ptp.util.ByteArrayUtil;
import ptp.util.URLUtil;

public class HttpProxy {

	private static Logger log = Logger.getLogger(HttpProxy.class);

	String destHost;
	int destPort;
	DataInputStream inFromBrowser;
	DataOutputStream outToBrowser;
	boolean isSSL;

	public HttpProxy(String destHost, int destPort,
			DataInputStream inFromBrowser, DataOutputStream outToBrowser,
			boolean isSSL) {
		this.destHost = destHost;
		this.destPort = destPort;
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
		this.isSSL = isSSL;
	}

	private void requestRemote(byte[] data, String destHost, int destPort,
			DataOutputStream outToBrowser) throws IOException {
		String remotePhp = Config.getIns().getRemotePhp();
		log.info("remotePhp: " + remotePhp);

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

		byte key = (byte) ((Math.random() * (100)) + 1);

		byte[] postData = ("request_data=" + requestEncodedString
				+ "&dest_host=" + destHostEncodedString + "&dest_port="
				+ destPort + "&is_ssl=" + isSSL + "&key=" + key).getBytes();

		log.debug("request: "
				+ ByteArrayUtil.toString(postData, 0, postData.length));

		URL remotePhpUrl = new URL(remotePhp);
		HttpURLConnection remotePhpConn = (HttpURLConnection) remotePhpUrl
				.openConnection(Config.getIns().getProxy());
		remotePhpConn.setRequestMethod("POST");
		remotePhpConn.setRequestProperty("User-Agent", Config.getIns()
				.getUserAgent());
		remotePhpConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		remotePhpConn.setRequestProperty("Connection", "close");
		remotePhpConn.setUseCaches(false);
		remotePhpConn.setDoOutput(true);

		DataOutputStream outToPhp = new DataOutputStream(remotePhpConn
				.getOutputStream());

		outToPhp.write(postData);

		outToPhp.flush();
		outToPhp.close();

		DataInputStream inFromPhp = new DataInputStream(remotePhpConn
				.getInputStream());
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

		inFromPhp.close();
	}

	public void proces(String[] requestHeaders, byte[] requestBody)
			throws IOException {
		StringBuilder newRequestHeaderString = new StringBuilder();

		for (int i = 0; i < requestHeaders.length; i++) {
			if (i == 0) {
				String method = requestHeaders[0].split("\\s")[0];
				String resourc = URLUtil.getResource(requestHeaders[0]
						.split("\\s")[1]);
				log.info("destPath: " + resourc);
				String version = requestHeaders[0].split("\\s")[2];
				newRequestHeaderString.append(method).append(" ").append(
						resourc).append(" ").append(version).append("\r\n");
			} else if (requestHeaders[i].startsWith("Proxy-Connection")) {
				continue;
			} else if (requestHeaders[i].startsWith("Connection")) {
				continue;
			} else if (requestHeaders[i].startsWith("Keep-Alive")) {
				continue;
			} else {
				newRequestHeaderString.append(requestHeaders[i]).append("\r\n");
			}
		}
		newRequestHeaderString.append("Connection: close").append("\r\n");
		newRequestHeaderString.append("\r\n");

		log.debug("Request Headers: ");
		log.debug(newRequestHeaderString);

		byte[] newRequestHeaderData = newRequestHeaderString.toString()
				.getBytes("US-ASCII");
		byte[] newRequestData = new byte[newRequestHeaderData.length
				+ requestBody.length];
		ByteArrayUtil.copy(newRequestHeaderData, 0, newRequestData, 0,
				newRequestHeaderData.length);
		ByteArrayUtil.copy(requestBody, 0, newRequestData,
				newRequestHeaderData.length, requestBody.length);

		requestRemote(newRequestData, destHost, destPort, outToBrowser);
		log.info("http proxy done!");
	}
}
