package cc.co.phptunnelproxy.ptplocal.net.mp;

import static org.junit.Assert.*;

import org.junit.Test;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;

public class HttpHeadReqLineTest {

	@Test
	public void testGetDestResource() {
		try {
			HttpHead hh = new HttpHead("GET http://ad-g.doubleclick.net/adi/com.picasa.myphotos/gallery;tile=1;sz=250x250;ord=7442045006901026;domain=picasaweb.google.com;myd=lhid_dc_userhome_ad HTTP/1.1");
			assertTrue("/adi/com.picasa.myphotos/gallery;tile=1;sz=250x250;ord=7442045006901026;domain=picasaweb.google.com;myd=lhid_dc_userhome_ad".equals(hh.getDestResource()));
		} catch (ProxyException e) {
			fail(e.getMessage());
		}
	}

}
