package ptp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {
	private static Logger log = Logger.getLogger(Config.class);

	private static Config ins = new Config();
	
	Properties prop = null;
	Properties ipMap = null;

	private Config() {
		prop = new Properties();
		ipMap = new Properties();
		try {
			FileInputStream fis = new FileInputStream("etc/ptp.properties");
			prop.load(fis);
			
			fis = new FileInputStream("etc/ipmap.properties");
			ipMap.load(fis);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static synchronized Config getIns() {
		return ins;
	}
	
	public String getValue(String key) {
		return prop.getProperty(key);
	}
	
	public String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	public String getRemotePhp() {
		int num = Integer.parseInt((this.getValue("ptp.remote.php.num")));
		int index = (int)(Math.random()*(num))+1;
		log.debug(index);
		log.debug(this.getValue("ptp.remote.php."+index));
		return this.getValue("ptp.remote.php."+index);
	}
	
	public String getIp(String domain) {
		return ipMap.getProperty(domain, domain);
	}

}
