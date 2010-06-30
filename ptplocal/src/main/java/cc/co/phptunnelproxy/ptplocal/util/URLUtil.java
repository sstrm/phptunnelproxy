package cc.co.phptunnelproxy.ptplocal.util;

public class URLUtil {

	public static String getResource(String str) {
		int index = -1;
		if(str.startsWith("http")) {
			index = str.indexOf(':');
			str = str.substring(index + 3);
		}

		index = str.indexOf('/');
		str = str.substring(index);

		return str;
	}

}
