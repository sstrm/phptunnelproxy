package cc.co.phptunnelproxy.ptplocal.net.mp.http;

import java.io.InputStream;

public abstract class HttpStartLine {

	protected String line;

	public HttpStartLine(String line) {
		this.line = line;
	}
	
	public HttpStartLine(InputStream in, byte key) {
		
	}

}
