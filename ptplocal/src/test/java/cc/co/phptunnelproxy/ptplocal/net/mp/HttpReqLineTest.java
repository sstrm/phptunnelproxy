package cc.co.phptunnelproxy.ptplocal.net.mp;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpReqLine;

public class HttpReqLineTest {

	@Test
	public void testGetDestResource() {
		try {
			HttpReqLine reqLine = new HttpReqLine(
					"GET http://ad-g.doubleclick.net/adi/com.picasa.myphotos/gallery;tile=1;sz=250x250;ord=7442045006901026;domain=picasaweb.google.com;myd=lhid_dc_userhome_ad HTTP/1.1");
			assertTrue("/adi/com.picasa.myphotos/gallery;tile=1;sz=250x250;ord=7442045006901026;domain=picasaweb.google.com;myd=lhid_dc_userhome_ad"
					.equals(reqLine.getDestResource()));
		} catch (HttpParseException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPacDestResource() {
		try {
			HttpReqLine reqLine = new HttpReqLine(
					"GET / HTTP/1.1");
			assertTrue("/"
					.equals(reqLine.getDestResource()));
		} catch (HttpParseException e) {
			fail(e.getMessage());
		}
	}

}
