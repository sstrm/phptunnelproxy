package ptp.net.http;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.util.ByteArrayUtil;

public class ResponseHttpMessage extends HttpMessage {

	protected byte key;

	protected int httpCode;

	protected boolean bodyReadEnd = false;
	
	protected boolean stoppedReadBody = false;
	
	private Thread httpBodyReadThread = null;

	private ResponseHttpMessage() {
		super();
	}

	public ResponseHttpMessage(byte key) {
		this();
		this.key = key;

	}

	@Override
	protected void readHttpHeaders(InputStream in) throws ProxyException {
		super.readHttpHeaders(in, key);
		String[] tokens = firstLine.split("\\s");

		this.version = tokens[0];
		this.httpCode = Integer.parseInt(tokens[1]);
	}

	@Override
	protected void readHttpBody(InputStream in) throws ProxyException {
		FileOutputStream out  = this.getBodyDataFileOutputStream();
		
		httpBodyReadThread = new HttpBodyReadThread(this, out, in);
		httpBodyReadThread.start();
	}

	public boolean isBodyReadEnd() {
		return this.bodyReadEnd;
	}

	public void stopReadBody() throws ProxyException {
		this.stoppedReadBody = true;
		if(this.httpBodyReadThread != null) {
			try {
				this.httpBodyReadThread.join();
			} catch (InterruptedException e) {
				throw new ProxyException(e);
			}
		}
	}

}

class HttpBodyReadThread extends Thread {
	private static Logger log = Logger.getLogger(HttpBodyReadThread.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	private ResponseHttpMessage rhm;
	private OutputStream out;
	private InputStream in;

	public HttpBodyReadThread(ResponseHttpMessage rhm, OutputStream out, InputStream in) {
		this.rhm = rhm;
		this.out = out;
		this.in = in;
	}

	public void run() {

		byte[] buff = new byte[buff_size];

		int readCount = 0;

		try {
			while ((readCount = in.read(buff, 0, buff_size)) != -1) {
				ByteArrayUtil.decrypt(buff, 0, readCount, rhm.key);
				out.write(buff, 0, readCount);
				if(rhm.stoppedReadBody) {
					break;
				}
			}
			out.close();
			rhm.bodyReadEnd = true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}