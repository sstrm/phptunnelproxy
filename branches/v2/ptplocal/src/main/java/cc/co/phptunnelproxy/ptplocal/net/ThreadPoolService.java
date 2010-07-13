package cc.co.phptunnelproxy.ptplocal.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolService {

	private static ExecutorService es = null;

	public static void startService() {
		es = Executors.newFixedThreadPool(20);
	}

	public static void execute(Runnable command) {
		es.execute(command);
	}

	public static void stopService() {
		es.shutdown();
	}

}
