package cc.co.phptunnelproxy.ptplocal.net.mp.http;

public class HttpParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public HttpParseException() {
		super();
	}

	public HttpParseException(Throwable t) {
		super(t);
	}

	public HttpParseException(String msg) {
		super(msg);
	}

}
