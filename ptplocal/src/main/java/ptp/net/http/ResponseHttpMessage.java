package ptp.net.http;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.ByteArrayUtil;

public class ResponseHttpMessage extends HttpMessage {
	private static Logger log = Logger.getLogger(ResponseHttpMessage.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.buff.size", "102400"));

	private byte key;

	protected int httpCode;

	private ResponseHttpMessage() {
		super();
	}

	public ResponseHttpMessage(byte key) {
		this();
		this.key = key;

	}

	@Override
	protected void readHttpHeaders(InputStream in) {
		super.readHttpHeaders(in, key);
		String[] tokens = firstLine.split("\\s");

		this.version = tokens[0];
		this.httpCode = Integer.parseInt(tokens[1]);
	}

	@Override
	protected void readHttpBody(InputStream in) {
		FileOutputStream bodyDataTmpFOS = this.getBodyDataFileOutputStream();

		byte[] buff = new byte[buff_size];

		int readCount = 0;

		try {
			while ((readCount = in.read(buff, 0, buff_size)) != -1) {
				ByteArrayUtil.decrypt(buff, 0, readCount, key);
				bodyDataTmpFOS.write(buff, 0, readCount);
			}
			bodyDataTmpFOS.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}
