package ptp.ui;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

public abstract class Launcher {
	
	private static String costomerLog4j = "etc/log4j.properties";
	static {
		File log4jFile = new File(costomerLog4j);
		if(log4jFile.exists()) {
			PropertyConfigurator.configure(costomerLog4j);
		} else {
			PropertyConfigurator.configure(Launcher.class
					.getResource("/etc/log4j.properties"));
		}
	}

}
