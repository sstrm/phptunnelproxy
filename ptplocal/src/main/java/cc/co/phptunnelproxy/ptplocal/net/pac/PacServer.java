package cc.co.phptunnelproxy.ptplocal.net.pac;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServer;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServerProcessThread;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpHead;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpReqLine;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpResLine;
import cc.co.phptunnelproxy.ptplocal.util.Base64Coder;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;

public class PacServer extends AbstractServer {
	private static Logger log = Logger.getLogger(PacServer.class);

	PacServerThread pacServerThread = null;

	public PacServer() {
		pacServerThread = new PacServerThread();
	}

	public int startService() {
		pacServerThread.start();
		return pacServerThread.pacPort;
	}

	public void stopService() {
		pacServerThread.shutdown();
		try {
			pacServerThread.join();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean isServerOn() {
		return pacServerThread.isAlive();
	}

}

class PacServerThread extends AbstractServerProcessThread {
	private static Logger log = Logger.getLogger(PacServerThread.class);

	int pacPort;

	private boolean isStopped = false;

	public synchronized void shutdown() {
		isStopped = true;
	}

	@Override
	public void run() {
		ServerSocket sSocket = null;
		try {
			pacPort = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.pac.port", "8888"));
			sSocket = new ServerSocket(pacPort);
			sSocket.setSoTimeout(1000);
			log.info("pac server started on port: " + pacPort);

			while (!isStopped) {
				Socket browserSocket = null;
				try {
					browserSocket = sSocket.accept();

					log.info("pac visit from browser: "
							+ browserSocket.getInetAddress().getHostAddress()
							+ " " + browserSocket.getPort());

					InputStream inFromBrowser = browserSocket.getInputStream();
					OutputStream outToBrowser = browserSocket.getOutputStream();
					
					HttpReqLine reqLine = null;
					HttpResLine resLine = null;

					HttpHead reqHH = null;
					HttpHead resHH = null;
					try {
						reqLine = new HttpReqLine(inFromBrowser);
						resLine = new HttpResLine("HTTP/1.1 200 OK");
						reqHH = new HttpHead(inFromBrowser);
						resHH = new HttpHead();
					} catch (HttpParseException e) {
						log.error(e.getMessage(), e);
						this.writeErrorResponse(outToBrowser, e, this.getClass());
						return;
					}

					String pacRequestPath = reqLine.getDestResource();
					String pacHostName = reqHH.getHeader("Host");
					if(pacHostName.contains(":")) {
						pacHostName = pacHostName.split(":")[0];
					}
					
					String resBody = null;

					if (pacRequestPath.equalsIgnoreCase("/gfwlist.txt")) {
						String gfwlist = this.getGFWList();
						resHH.setHeader("Content-Length",
								Integer.toString(gfwlist.length()));
						resHH.setHeader("Content-Type", "text/plain");
						resBody = gfwlist;
					} else if (pacRequestPath.equalsIgnoreCase("/rule.txt")) {
						String rule = this.getRule();
						resHH.setHeader("Content-Length",
								Integer.toString(rule.length()));
						resHH.setHeader("Content-Type", "text/plain");
						resBody = rule;
					} else if (pacRequestPath.equalsIgnoreCase("/gfwlist.pac")) {
						String pac = this.getPac(
								pacHostName,
								Integer.parseInt(Config.getIns().getValue(
										"ptp.local.proxy.port", "8888")));
						resHH.setHeader("Content-Length",
								Integer.toString(pac.length()));
						resHH.setHeader("Content-Type", "text/plain");
						resBody = pac;
					} else {
						URL pacIndexUrl = PacServer.class
								.getResource("/etc/pacindex.html");
						URLConnection pacIndexUrlConn = pacIndexUrl
								.openConnection();

						resHH.setHeader("Content-Length", Integer
								.toString(pacIndexUrlConn.getContentLength()));
						resHH.setHeader("Content-Type", "text/html");

						BufferedReader pacIndexR = new BufferedReader(
								new InputStreamReader(
										pacIndexUrlConn.getInputStream()));

						String m = null;
						StringBuilder sb = new StringBuilder();
						while ((m = pacIndexR.readLine()) != null) {
							sb.append(m).append("\n");
						}
						resBody = sb.toString();

					}

					outToBrowser.write(resLine.getBytes());
					outToBrowser.write(resHH.getBytes());
					outToBrowser.write(ByteArrayUtil
							.getBytesFromString(resBody));
					outToBrowser.flush();

				} catch (SocketTimeoutException ste) {
					continue;
				}
				if (browserSocket != null) {
					browserSocket.close();
				}
			}
			sSocket.close();
			log.info("stop pac server");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getGFWList() {
		BufferedReader gfwlistR = null;
		try {
			URL gfwlistUrl = new URL(Config.getIns().getValue(
					"ptp.local.pac.gfwlist",
					"http://autoproxy-gfwlist.googlecode.com"
							+ "/svn/trunk/gfwlist.txt"));
			log.info("gfwlist: " + gfwlistUrl.toString());
			URLConnection gfwlistConn = gfwlistUrl.openConnection(Config
					.getIns().getProxy());
			gfwlistConn.addRequestProperty("User-Agent", Config.getIns()
					.getUserAgent());
			gfwlistR = new BufferedReader(new InputStreamReader(
					gfwlistConn.getInputStream()));
		} catch (IOException e1) {
			log.error(e1.getMessage(), e1);
			URL gfwlistUrl = PacServer.class.getResource("/etc/gfwlist.txt");
			log.info("gfwlist: " + gfwlistUrl.toString());
			URLConnection gfwlistConn;
			try {
				gfwlistConn = gfwlistUrl.openConnection();
				gfwlistR = new BufferedReader(new InputStreamReader(
						gfwlistConn.getInputStream()));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return "";
			}
		}

		StringBuilder gfwlistContent = new StringBuilder();

		try {
			String line = null;
			while ((line = gfwlistR.readLine()) != null) {
				gfwlistContent.append(line).append("\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return "";
		}

		return gfwlistContent.toString();
	}

	private String getRule() {
		String ruleString = Base64Coder.decodeString(this.getGFWList().replace(
				"\n", ""));
		return ruleString;
	}

	private String getPac(String hostName, int port) {
		StringBuilder pacContent = new StringBuilder();

		try {

			BufferedReader ruleR = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(this.getRule().getBytes(
							"ISO-8859-1"))));

			pacContent.append("function FindProxyForURL(url, host) {").append(
					"\n");
			pacContent
					.append("\t")
					.append("var PROXY = \"PROXY " + hostName + ":" + port
							+ "\";").append("\n");
			pacContent.append("\t").append("var DEFAULT = \"DIRECT\";")
					.append("\n");

			String line = null;

			File customRuleFile = new File("etc/gfwlist.txt");
			if (customRuleFile.exists()) {
				BufferedReader customeRuleR = new BufferedReader(
						new InputStreamReader(new FileInputStream(
								customRuleFile)));
				while ((line = customeRuleR.readLine()) != null) {
					pacContent.append(convertRule(line));
				}
			}

			while ((line = ruleR.readLine()) != null) {
				pacContent.append(convertRule(line));
			}
			pacContent.append("\t").append("return DEFAULT;").append("\n");
			pacContent.append("}\n");

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		return pacContent.toString();
	}

	private String convertRule(String ruleLine) {
		log.debug(ruleLine);
		if (ruleLine.isEmpty()) {
			return "";
		}

		if (ruleLine.startsWith("[") || ruleLine.startsWith("!")) {
			return "";
		}

		String returnProxy = "PROXY";
		String ruleReg = "";

		if (ruleLine.startsWith("@@")) {
			ruleLine = ruleLine.substring(2);
			returnProxy = "DEFAULT";
		}

		if (ruleLine.startsWith("||")) {
			ruleReg = "/^[\\w\\-]+:\\/+(?!\\/)(?:[^\\/]+\\.)?"
					+ regEncode(ruleLine.substring(2)) + "/";
		} else if (ruleLine.startsWith("|")) {
			ruleReg = "/^" + regEncode(ruleLine.substring(1)) + "/";
		} else if (ruleLine.startsWith("/") && ruleLine.endsWith("/")) {
			ruleReg = ruleLine;
		} else {
			ruleReg = "/" + regEncode(ruleLine) + "/";
		}

		StringBuilder pacLine = new StringBuilder();
		pacLine.append("\t").append("if(").append(ruleReg)
				.append("i.test(url)) return ").append(returnProxy)
				.append(";\n");
		return pacLine.toString();

	}

	private String regEncode(String str) {
		return str.replace("/", "\\/").replace(".", "\\.").replace(":", "\\:")
				.replace("%", "\\%").replace("*", ".*").replace("-", "\\-")
				.replace("&", "\\&").replace("?", "\\?");
	}
}
