package ptp.ui;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.local.LocalProxyServer;
import ptp.local.SSLForwardServer;

public class GUILauncher extends Launcher {
	private static Logger log = Logger.getLogger(GUILauncher.class);

	private static Thread sslForwarderThread;
	private static SSLForwardServer sslForwardServer;
	private static Thread localProxyThread;
	private static LocalProxyServer localProxyServer;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createUI();
	}

	public static void startServer() {
		sslForwardServer = new SSLForwardServer();
		sslForwarderThread = new Thread(sslForwardServer);
		sslForwarderThread.start();

		localProxyServer = new LocalProxyServer();
		localProxyThread = new Thread(localProxyServer);
		localProxyThread.start();
	}

	public static void stopServer() {
		if (localProxyServer == null || localProxyThread == null
				|| sslForwardServer == null || sslForwarderThread == null) {
			return;
		}

		localProxyServer.stopServer();
		try {
			localProxyThread.join();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}

		sslForwardServer.stopServer();
		try {
			sslForwarderThread.join();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		log.info("server stop successfully!");
	}

	public static void createUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		JComponent.setDefaultLocale(new Locale(""));

		final ImageIcon onImgIcon = new ImageIcon(JXTrayIcon.class
				.getResource("/res/tray-on.png"));
		final ImageIcon offImgIcon = new ImageIcon(JXTrayIcon.class
				.getResource("/res/tray-off.png"));
		final ImageIcon logImgIcon = new ImageIcon(JXTrayIcon.class
				.getResource("/res/log.png"));
		final ImageIcon infoImgIcon = new ImageIcon(JXTrayIcon.class
				.getResource("/res/info.png"));

		JTextArea logMessageTextArea = new JTextArea();
		logMessageTextArea.setEditable(false);
		logMessageTextArea.setFont(new Font(Config.getIns().getValue(
				"ptp.local.gui.log.font", Font.MONOSPACED), Font.PLAIN, 12));
		JScrollPane logMessageScrollTextAreas = new JScrollPane(
				logMessageTextArea);
		logMessageScrollTextAreas
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logMessageScrollTextAreas
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		final JFrame logWindow = new JFrame("PHP Tunnel Log");
		logWindow.setVisible(false);
		logWindow.setSize(600, 400);
		logWindow.setLocationRelativeTo(null);
		logWindow.setIconImage(logImgIcon.getImage());
		logWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		logWindow.add(logMessageScrollTextAreas);

		JTextAreaAppender guiAppender = (JTextAreaAppender) Logger
				.getRootLogger().getAppender("gui");
		if (guiAppender != null) {
			guiAppender.setTextArea(logMessageTextArea);
		}

		final JXTrayIcon tray = new JXTrayIcon(offImgIcon.getImage());

		// create pop up menu
		final JPopupMenu popupMenu = new JPopupMenu();
		final JMenuItem switchItem = new JMenuItem("Start", onImgIcon);

		ActionListener switchAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (switchItem.getText().equals("Start")) {
					startServer();
					switchItem.setText("Stop");
					switchItem.setIcon(offImgIcon);
					tray.setImage(onImgIcon.getImage());
				} else if (switchItem.getText().equals("Stop")) {
					stopServer();
					switchItem.setText("Start");
					switchItem.setIcon(onImgIcon);
					tray.setImage(offImgIcon.getImage());
				}
			}
		};

		switchItem.addActionListener(switchAction);

		popupMenu.add(switchItem);

		final JMenuItem logItem = new JMenuItem("Log", new ImageIcon(
				JXTrayIcon.class.getResource("/res/log.png")));
		logItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!logWindow.isVisible()) {
					logWindow.setExtendedState(JFrame.NORMAL);
					logWindow.setVisible(true);
					logWindow.toFront();
				} else {
					logWindow.setExtendedState(JFrame.NORMAL);
					logWindow.toFront();
				}

			}
		});
		popupMenu.add(logItem);

		final JMenuItem aboutItem = new JMenuItem("About", new ImageIcon(
				JXTrayIcon.class.getResource("/res/about.png")));
		aboutItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel messagePanel = new JPanel();
				messagePanel.setLayout(new BoxLayout(messagePanel,
						BoxLayout.Y_AXIS));
				JLabel versionLabel = new JLabel("PHP Tunnel Proxy Local "
						+ Config.getIns().getVersion() + " "
						+ Config.getIns().getCompileDate());
				JLabel linkLabel = new JLabel(
						"<html>"
								+ "<a href=\"http://code.google.com/p/phptunnelproxy/\">"
								+ "http://code.google.com/p/phptunnelproxy/</a>"
								+ "</html>");
				linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {

					public void mouseClicked(MouseEvent e) {
						try {
							Desktop
									.getDesktop()
									.browse(
											new URI(
													"http://code.google.com/p/phptunnelproxy/"));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});

				linkLabel.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));

				messagePanel.add(versionLabel);
				messagePanel.add(linkLabel);

				tray.setCanPopup(false);
				JOptionPane.showMessageDialog(null, messagePanel, "About",
						JOptionPane.INFORMATION_MESSAGE, infoImgIcon);
				tray.setCanPopup(true);
			}
		});

		popupMenu.add(aboutItem);

		final JMenuItem exitItem = new JMenuItem("Exit", new ImageIcon(
				JXTrayIcon.class.getResource("/res/exit.png")));
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopServer();
				System.exit(0);
			}
		});
		popupMenu.add(exitItem);

		tray.setJPopupMenu(popupMenu);
		tray.addActionListener(switchAction);
		tray.setToolTip("PHP Tunnel Proxy Local");
		try {
			SystemTray.getSystemTray().add(tray);
		} catch (AWTException e) {
			log.error(e.getMessage(), e);
		}
	}

}
