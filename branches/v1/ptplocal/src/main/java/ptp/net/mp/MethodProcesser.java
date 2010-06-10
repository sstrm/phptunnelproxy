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

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.http.HttpMessage;
import ptp.net.http.RequestHttpMessage;
import ptp.net.http.ResponseHttpMessage;
import ptp.util.Base64Coder;
import ptp.util.ByteArrayUtil;

public class MethodProcesser {
	private static Logger log = Logger.getLogger(MethodProcesser.class);

	public enum MethodType {
		GET, POST, DELETE, PUT, HEAD;

		// public MethodType valueOf(String methodName) {
		// if (methodName.toUpperCase().equals("GET")) {
		// return GET;
		// } else if (methodName.toUpperCase().equals("POST")) {
		// return POST;
		// } else if (methodName.toUpperCase().equals("DELETE")) {
		// return DELETE;
		// } else if (methodName.toUpperCase().equals("PUT")) {
		// return PUT;
		// } else {
		// return HEAD;
		// }
		// }

	}

	// private MethodProcesser(){}

	private RequestHttpMessage reqHm;

	public MethodProcesser(RequestHttpMessage reqHm) {
		this.reqHm = reqHm;
	}

	HttpMessage requestRemote(RequestHttpMessage hm, boolean isSSL) {
		byte[] data = hm.getBytes();

		String requestBase64String = new String(Base64Coder.encode(data, 0,
				data.length));
		String destHostBase64String = Base64Coder.encodeString(hm.getHost());

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

		try {
			postData = ("request_data=" + requestEncodedString + "&dest_host="
					+ destHostEncodedString + "&dest_port=" + hm.getPort() + "&is_ssl="
					+ isSSL + "&key=" + key).getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		log.debug("request: "
				+ ByteArrayUtil.toString(postData, 0, postData.length));

		String remotePhp = Config.getIns().getRemotePhp();
		log.info("remotePhp: " + remotePhp);
		URL remotePhpUrl = null;
		try {
			remotePhpUrl = new URL(remotePhp);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpURLConnection remotePhpConn = null;
		try {
			remotePhpConn = (HttpURLConnection) remotePhpUrl
					.openConnection(Config.getIns().getProxy());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO
		}

		InputStream inFromPhp = null;
		try {
			inFromPhp = remotePhpConn.getInputStream();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
		ResponseHttpMessage resHm = new ResponseHttpMessage(key);
		resHm.read(inFromPhp);
		resHm.removeHeader("Connection");
		resHm.setHeader("Proxy-Connection", "keep-alive");
		resHm.setHeader("X-Proxy-Server", Config.getIns().getUserAgent());
		resHm.setHeader("X-PTP-Thread-Name", Thread.currentThread().getName());
		resHm.setHeader("X-PTP-Remote-PHP", remotePhpUrl.toString());
		resHm.setHeader("X-PTP-Key", String.valueOf(key));
		
		try {
			inFromPhp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resHm;
	}

	public HttpMessage process() {
		reqHm.removeHeader("Proxy-Connection");
		reqHm.removeHeader("Keep-Alive");
		reqHm.setHeader("Connection", "close");

		return this.requestRemote((RequestHttpMessage) reqHm, false);
	}

}
