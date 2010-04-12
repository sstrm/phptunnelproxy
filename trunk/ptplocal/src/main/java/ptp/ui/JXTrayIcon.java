package ptp.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Thanks to widgetfx project
 * http://code.google.com/p/widgetfx/source/browse/trunk/container/src/
 * org/widgetfx/ui/JXTrayIcon.java?r=187
 */
public class JXTrayIcon extends TrayIcon {

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

	private JPopupMenu menu;

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
}
