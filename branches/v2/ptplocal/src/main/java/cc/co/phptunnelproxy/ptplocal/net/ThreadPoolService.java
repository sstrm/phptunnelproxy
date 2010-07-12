package cc.co.phptunnelproxy.ptplocal.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolService {

	private static ExecutorService es = null;

	public static void startService() {
		es = Executors.newFixedThreadPool(20);
	}

	public static Future<?> submit(Runnable task) {
		return es.submit(task);
	}

	public static void stopService() {
		es.shutdown();
	}

}
