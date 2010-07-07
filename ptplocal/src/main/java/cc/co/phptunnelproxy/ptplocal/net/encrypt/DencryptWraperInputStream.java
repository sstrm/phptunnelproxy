package cc.co.phptunnelproxy.ptplocal.net.encrypt;

import java.io.IOException;
import java.io.InputStream;

public class DencryptWraperInputStream extends InputStream {
	private InputStream wraperedIn;
	private int key;

	DencryptWraperInputStream(InputStream wraperedIn, int key) {
		this.wraperedIn = wraperedIn;
		this.key = key;
	}

	@Override
	public int read() throws IOException {
		int b = wraperedIn.read();
		if (b == -1) {
			return -1;
		} else
			return b - key;
	}

	@Override
	public long skip(long n) throws IOException {
		return wraperedIn.skip(n);
	}

	@Override
	public int available() throws IOException {
		return wraperedIn.available();
	}

	@Override
	public void close() throws IOException {
		wraperedIn.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		wraperedIn.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		wraperedIn.reset();
	}

	@Override
	public boolean markSupported() {
		return wraperedIn.markSupported();
	}
}
