package cc.co.phptunnelproxy.ptplocal.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DumpUtil {

	public static String dump(byte[] array, int off, int length) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (int i = 0; i < length; i += 8) {
			pw.println();

			for (int j = i; j < (i + 8 > length ? length : i + 8); j++) {
				pw.format("%1$02x ", array[j]);
			}

			for (int j = (i + 8 > length ? length : i + 8); j < i + 8; j++) {
				pw.print("   ");
			}
			pw.print(" | ");
			for (int j = i; j < (i + 8 > length ? length : i + 8); j++) {
				int c = array[j] & 0xFF;
				if (c == '\r' || c == '\n') {
					c = ' ';
				}
				pw.format("%c ", c);
			}
		}
		return sw.getBuffer().toString();
	}

}
