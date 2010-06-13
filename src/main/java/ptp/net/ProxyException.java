package ptp.net;

public class ProxyException extends Exception {

	public ProxyException() {
		super();
	}

	public ProxyException(Throwable t) {
		super(t);
	}
	
	public ProxyException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 1L;

}
