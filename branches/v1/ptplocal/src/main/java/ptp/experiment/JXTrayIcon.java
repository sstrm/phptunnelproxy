package ptp.experiment;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JXTrayIcon extends TrayIcon {
	static {
		PropertyConfigurator.configure(JXTrayIcon.class
				.getResource("/etc/log4j.properties"));
	}
	private static Logger log = Logger.getLogger(JXTrayIcon.class);
	private JPopupMenu menu;
	private static JDialog dialog;
	static {
		dialog = new JDialog((Frame) null, "TrayDialog");
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
	}

	private static PopupMenuListener popupListener = new PopupMenuListener() {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			dialog.setVisible(false);
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
			dialog.setVisible(false);
		}
	};

	public JXTrayIcon(Image image) {
		super(image);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showJPopupMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				showJPopupMenu(e);
			}
		});
	}

	private void showJPopupMenu(MouseEvent e) {
		if (e.isPopupTrigger() && menu != null) {
			Dimension size = menu.getPreferredSize();
			dialog.setLocation(e.getX(), e.getY() - size.height);
			dialog.setVisible(true);
			menu.show(dialog.getContentPane(), 0, 0);
			// popup works only for focused windows
			dialog.toFront();
		}
	}

	public JPopupMenu getJPopupMenu() {
		return menu;
	}

	public void setJPopupMenu(JPopupMenu menu) {
		if (this.menu != null) {
			this.menu.removePopupMenuListener(popupListener);
		}
		this.menu = menu;
		menu.addPopupMenuListener(popupListener);
	}

	private static void createGui() {
		URL imgURL = SystemTrayDemo3.class.getResource("/res/tray-off.png");
		ImageIcon img = new ImageIcon(imgURL);
		JXTrayIcon tray = new JXTrayIcon(img.getImage());
		tray.setJPopupMenu(createJPopupMenu());
		try {
			SystemTray.getSystemTray().add(tray);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Metal".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
			}
			log.info(info.getName());
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGui();
			}
		});
	}

	/*
	 * static Image createImage() { BufferedImage i = new BufferedImage(32, 32,
	 * BufferedImage.TYPE_INT_ARGB); Graphics2D g2 = (Graphics2D)
	 * i.getGraphics(); g2.setColor(Color.RED); g2.fill(new Ellipse2D.Float(0,
	 * 0, i.getWidth(), i.getHeight())); g2.dispose(); return i; }
	 */

	static JPopupMenu createJPopupMenu() {
		final JPopupMenu m = new JPopupMenu();
		JMenuItem switchItem = new JMenuItem("Item 1", new ImageIcon(
				SystemTrayDemo3.class.getResource("/res/log.png")));
		m.add(switchItem);
		m.add(new JMenuItem("Item 2", new ImageIcon(SystemTrayDemo3.class
				.getResource("/res/options.png"))));
		JMenu submenu = new JMenu("Submenu");
		submenu.add(new JMenuItem("item 1"));
		submenu.add(new JMenuItem("item 2"));
		submenu.add(new JMenuItem("item 3"));
		m.add(submenu);
		JMenuItem exitItem = new JMenuItem("Exit", new ImageIcon(
				SystemTrayDemo3.class.getResource("/res/stop.png")));
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		m.add(exitItem);
		return m;
	}
}
