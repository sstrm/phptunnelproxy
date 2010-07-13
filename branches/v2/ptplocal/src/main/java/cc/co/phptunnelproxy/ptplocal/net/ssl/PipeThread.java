package cc.co.phptunnelproxy.ptplocal.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;

public class PipeThread implements Runnable {
	private static Logger log = Logger.getLogger(PipeThread.class);

	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	private InputStream in;
	private OutputStream out;
	private String title;

	private Lock lock;
	private Condition condition;
	private boolean isPipebegin = false;

	public PipeThread(InputStream in, OutputStream out, String title) {
		this.in = in;
		this.out = out;
		this.title = title;
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	@Override
	public void run() {
		lock.lock();
		try {
			isPipebegin = true;
			condition.signalAll();

			int rc = 0;

			byte[] buff = new byte[buff_size];
			try {
				while (true) {
					rc = in.read(buff);
					if (rc == -1) {
						break;
					}
					if (rc > 0) {
						out.write(buff, 0, rc);
						out.flush();
					}
				}
			} catch (SocketException e) {
				log.info(title + ":" + e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			} finally {
				try {
					in.close();
					out.close();
				} catch (final Exception e) {
				}
			}
		} finally {
			lock.unlock();
		}

	}

	public void join() {
		lock.lock();
		try {
			while (!isPipebegin) {
				try {
					condition.await();
				} catch (InterruptedException e) {
					//
				}
			}
		} finally {
			lock.unlock();
		}

	}

}