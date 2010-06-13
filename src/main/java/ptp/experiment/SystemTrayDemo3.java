package ptp.experiment;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class SystemTrayDemo3 {
	public static void main(String[] args) throws Exception {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		SystemTray tray = SystemTray.getSystemTray();
		URL imgURL = SystemTrayDemo3.class.getResource("/res/tray.png");
		ImageIcon img1 = new ImageIcon(imgURL);
		PopupMenu menu = new PopupMenu();
		MenuItem messageItem = new MenuItem("About");
		messageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "www.java2000.net");
			}
		});
		menu.add(messageItem);
		MenuItem closeItem = new MenuItem("Exit");
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		menu.add(closeItem);
		TrayIcon icon = new TrayIcon(img1.getImage(), "JAVA世纪网托盘演示", menu);
		icon.setImageAutoSize(true);
		tray.add(icon);
	}
}
