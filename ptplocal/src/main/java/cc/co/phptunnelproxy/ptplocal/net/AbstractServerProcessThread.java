package cc.co.phptunnelproxy.ptplocal.net;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public abstract class AbstractServerProcessThread extends Thread {

	public void writeErrorResponse(OutputStream outToBrowser,
			Exception e, Class<?> clazz) {
		Logger log = Logger.getLogger(clazz);
		log.error("wirite error page for: " + e.getMessage(),
				e);
		PrintWriter w = new PrintWriter(new OutputStreamWriter(outToBrowser));
		w.write("HTTP/1.1 500 Internal Server Error\r\n");
		w.write("Content-Type: text/html; charset=utf-8\r\n");
		w.write("Connection: close\r\n");
		w.write("\r\n");

		w.write("<html>");
		w.write("<head><title>HTTP 500 Internal Server Error</title><head>");
		w.write("<body><pre>");
		e.printStackTrace(w);
		w.write("</pre></body>");
		w.write("</html>");
		w.flush();
		w.close();
	}

}
