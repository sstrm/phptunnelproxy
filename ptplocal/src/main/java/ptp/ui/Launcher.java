package ptp.ui;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ptp.Config;
import ptp.net.pac.PacServer;
import ptp.net.proxy.HttpProxyServer;

public abstract class Launcher {
	private static Logger log = Logger.getLogger(Launcher.class);

	private static String costomerLog4j = "etc/log4j.properties";

	private static HttpProxyServer httpProxyServer;
	private static PacServer pacServer;

	static {
		File log4jFile = new File(costomerLog4j);
		if (log4jFile.exists()) {
			PropertyConfigurator.configure(costomerLog4j);
		} else {
			PropertyConfigurator.configure(Launcher.class
					.getResource("/etc/log4j.properties"));
		}

	}

	protected static void promot() {
		log.info(Config.getIns().getUserAgent() + " starts!");

		Properties sysProps = System.getProperties();
		log.info("java.runtime.version: "
				+ sysProps.getProperty("java.runtime.version"));
		log.info("java.vm.vendor: " + sysProps.getProperty("java.vm.vendor"));
		log.info("os.name: " + sysProps.getProperty("os.name"));
		log.info("os.version: " + sysProps.getProperty("os.version"));
	}

	public static synchronized void startServer() {
		pacServer = new PacServer();
		pacServer.startService();

		httpProxyServer = new HttpProxyServer();
		httpProxyServer.startService();
	}

	public static synchronized void stopServer() {
		if (httpProxyServer != null) {
			httpProxyServer.stopService();
		}

		if (pacServer != null) {
			pacServer.stopService();
		}
	}

}
