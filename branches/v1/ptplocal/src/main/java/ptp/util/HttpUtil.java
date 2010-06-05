package ptp.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

public class HttpUtil {
	private static Logger log = Logger.getLogger(HttpUtil.class);

	public static int readHttpHead(byte[] buff, InputStream in)
			throws IOException {
		int index = 0;
		int findEnd = 0;
		while (findEnd < 4) {
			byte b = 0;
			try {
				b = (byte) in.read();
			} catch (IOException e) {
				throw e;
			}
			buff[index++] = b;
			if (b == '\r' || b == '\n') {
				findEnd++;
			} else {
				findEnd = 0;
			}
		}

		return index;
	}

	public static void writeErrorResponse(OutputStream outToBrowser, String msg) {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
				outToBrowser));

		try {
			w.write("HTTP/1.0 500 OK");
			w.write("\r\n\r\n");

			w.write(msg);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}
