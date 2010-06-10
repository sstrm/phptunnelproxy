package ptp.net.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.mp.MethodProcesser;
import ptp.util.URLUtil;

public class RequestHttpMessage extends HttpMessage {
	private static Logger log = Logger.getLogger(RequestHttpMessage.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.buff.size", "102400"));

	protected MethodProcesser.MethodType methodType;

	protected String resource;

	protected String host;

	protected int port;

	@Override
	protected void readHttpHeaders(InputStream in) {
		super.readHttpHeaders(in, (byte) 0);
		String[] tokens = firstLine.split("\\s");
		this.methodType = MethodProcesser.MethodType.valueOf(tokens[0]);
		this.resource = URLUtil.getResource(tokens[1]);
		URL requestURL = null;
		try {
			requestURL = new URL(tokens[1]);
			log.info("request url: " + requestURL.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
		}

		host = requestURL.getHost();
		host = Config.getIns().getIp(host);

		port = requestURL.getPort() != -1 ? requestURL.getPort() : 80;

		this.version = tokens[2];
	}

	public MethodProcesser.MethodType getMethodType() {
		return this.methodType;
	}

	public String getResource() {
		return this.resource;
	}

	public String getVersion() {
		return this.version;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	protected void readHttpBody(InputStream in) {
		if (this.headers.containsKey("Content-Length")) {
			int contentLength = Integer.parseInt(this.headers
					.get("Content-Length"));

			FileOutputStream bodyDataTmpFOS = null;
			try {
				this.bodyDataFile = File.createTempFile(Thread.currentThread()
						.getName()
						+ new Date().getTime(), ".ptp");
				log.info("create body data file: "
						+ this.bodyDataFile.getAbsolutePath());
				bodyDataTmpFOS = new FileOutputStream(this.bodyDataFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buff = new byte[buff_size];

			int readCount = 0;

			try {
				while (readCount < contentLength) {
					int thisReadCount = in.read(buff, 0, buff_size);
					bodyDataTmpFOS.write(buff, 0, thisReadCount);
					readCount += thisReadCount;
					log.debug("postContentReadCount: " + readCount);
				}
				bodyDataTmpFOS.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
