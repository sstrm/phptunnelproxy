package cc.co.phptunnelproxy.ptplocal.net;

public abstract class AbstractServer {
	/**
	 * start server
	 * 
	 * @return service port
	 */
	public abstract int startService();

	/**
	 * stop server
	 */
	public abstract void stopService();

	/**
	 * check whether server is on
	 * 
	 * @return
	 */
	public abstract boolean isServerOn();

}
