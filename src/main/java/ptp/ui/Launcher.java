package ptp.ui;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public abstract class Launcher {
	private static Logger log = Logger.getLogger(Launcher.class);

	private static String costomerLog4j = "etc/log4j.properties";
	static {
		File log4jFile = new File(costomerLog4j);
		if (log4jFile.exists()) {
			PropertyConfigurator.configure(costomerLog4j);
		} else {
			PropertyConfigurator.configure(Launcher.class
					.getResource("/etc/log4j.properties"));
		}
	}

	public static void logSysProps() {
		Properties sysProps = System.getProperties();
		Enumeration<?> sysPropNames = sysProps.propertyNames();
		while (sysPropNames.hasMoreElements()) {
			String key = (String) sysPropNames.nextElement();
			log.debug("System Property: " + key + " = "
					+ sysProps.getProperty(key));
		}
	}

}
