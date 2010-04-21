package ptp.pac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.Base64Coder;

public class PacServer implements Runnable {
	private static Logger log = Logger.getLogger(PacServer.class);

	private boolean isStopped = false;

	public synchronized void stopServer() {
		isStopped = true;
	}

	@Override
	public void run() {
		ServerSocket sSocket = null;
		try {
			int pacPort = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.pac.port", "8888"));
			sSocket = new ServerSocket(pacPort);
			sSocket.setSoTimeout(1000);
			log.info("pac server started on port: " + pacPort);

			while (!isStopped) {
				Socket browserSocket = null;
				try {
					browserSocket = sSocket.accept();

					log.info("visit from browser: "
							+ browserSocket.getInetAddress().getHostAddress()
							+ " " + browserSocket.getPort());

					BufferedWriter w = new BufferedWriter(
							new OutputStreamWriter(browserSocket
									.getOutputStream()));
					BufferedReader r = new BufferedReader(
							new InputStreamReader(browserSocket
									.getInputStream()));
					String m = r.readLine();
					if (m != null) {
						w.write("HTTP/1.0 200 OK");
						w.newLine();

						String[] tokens = m.split("\\s");
						String pacRequestPath = tokens[1];
						if (pacRequestPath.equalsIgnoreCase("/gfwlist.txt")) {
							String gfwlist = this.getGFWList();
							w.write("Content-Length: " + gfwlist.length()
									+ "\r\n");
							w.write("Content-Type: text/plain\r\n\r\n");
							w.write(gfwlist);
						} else if (pacRequestPath.equalsIgnoreCase("/rule.txt")) {
							String rule = this.getRule();
							w
									.write("Content-Length: " + rule.length()
											+ "\r\n");
							w.write("Content-Type: text/plain\r\n\r\n");
							w.write(this.getRule());
						} else if (pacRequestPath
								.equalsIgnoreCase("/gfwlist.pac")) {
							String pac = this.getPac(Integer.parseInt(Config
									.getIns().getValue("ptp.local.proxy.port",
											"8888")));
							
							w.write("Content-Length: " + pac.length() + "\r\n");
							w.write("Content-Type: text/plain\r\n\r\n");
							w.write(pac);
						} else {
							StringBuilder sb = new StringBuilder();
							sb.append("<html>");
							sb.append("<head>").append(
									"<title>").append("PTP Pac Server").append(
									"</title>").append("</head>");
							sb.append("<body>");
							sb.append("<h1>PTP Pac Server</h1>").append("<br />");
							sb.append("<h3>Use one of them</h3>").append("<br />");
							sb.append("<a href=\"/gfwlist.txt\">/gfwlist.txt</a>").append(" for base64 encoded gfwlist.txt from gfwlist project").append("<br />");
							sb.append("<a href=\"/rule.txt\">/rule.txt</a>").append(" for base64 decoded gfwlist.txt").append("<br />");
							sb.append("<a href=\"/gfwlist.pac\">/gfwlist.pac</a>").append(" for pac script").append("<br />");
							
							w.write("Content-Length: " + sb.length() + "\r\n");
							w.write("Content-Type: text/html\r\n\r\n");
							w.write(sb.toString());
						}

						w.flush();
					}
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
					PacServer.class.getResource("/etc/gfwlist.txt").toString()));
			log.debug(Config.getIns().getValue("ptp.local.pac.gfwlist"));
			log.info("gfwlist: " + gfwlistUrl.toString());
			URLConnection gfwlistConn = gfwlistUrl.openConnection();
			gfwlistR = new BufferedReader(new InputStreamReader(gfwlistConn
					.getInputStream()));
		} catch (IOException e1) {
			log.error(e1.getMessage(), e1);
		}

		StringBuilder gfwlistContent = new StringBuilder();

		try {
			String line = null;
			while ((line = gfwlistR.readLine()) != null) {
				gfwlistContent.append(line).append("\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		return gfwlistContent.toString();
	}

	private String getRule() {
		String ruleString = Base64Coder.decodeString(this.getGFWList().replace(
				"\n", ""));
		return ruleString;
	}

	private String getPac(int port) {
		StringBuilder pacContent = new StringBuilder();

		try {

			BufferedReader ruleR = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(this.getRule().getBytes(
							"ISO-8859-1"))));

			pacContent.append("function FindProxyForURL(url, host) {").append(
					"\n");
			pacContent.append("\t").append(
					"var PROXY = \"PROXY 127.0.0.1:" + port + "\";").append(
					"\n");
			pacContent.append("\t").append("var DEFAULT = \"DIRECT\";").append(
					"\n");

			String line = null;
			while ((line = ruleR.readLine()) != null) {
				log.debug(line);
				if (line.isEmpty()) {
					continue;
				}

				if (line.startsWith("[") || line.startsWith("!")) {
					continue;
				}

				String returnProxy = "PROXY";
				String ruleReg = "";

				if (line.startsWith("@@")) {
					line = line.substring(2);
					returnProxy = "DEFAULT";
				}

				if (line.startsWith("||")) {
					ruleReg = "/^[\\w\\-]+:\\/+(?!\\/)(?:[^\\/]+\\.)?"
							+ regEncode(line.substring(2)) + "/";
				} else if (line.startsWith("|")) {
					ruleReg = "/^" + regEncode(line.substring(1)) + "/";
				} else if (line.startsWith("/") && line.endsWith("/")) {
					ruleReg = line;
				} else {
					ruleReg = "/" + regEncode(line) + "/";
				}
				pacContent.append("\t").append("if(").append(ruleReg).append(
						"i.test(url)) return ").append(returnProxy).append(
						";\n");
			}
			pacContent.append("\t").append("return DEFAULT;").append("\n");
			pacContent.append("}\n");

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		return pacContent.toString();
	}

	private String regEncode(String str) {
		return str.replace("/", "\\/").replace(".", "\\.").replace(":", "\\:")
				.replace("%", "\\%").replace("*", ".*").replace("-", "\\-")
				.replace("&", "\\&").replace("?", "\\?");
	}

	public static void main(String[] args) {
		System.out.println(new PacServer().regEncode("http://nsf.110mb.com"));
	}
}
