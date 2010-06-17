package ptp.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class SwtLauncher extends Launcher {

	public static void main(String[] args) {
		Display display = new Display();

		final Shell shell = new Shell(display);

		shell.setText("PTP Local Shell");

		ImageData imageData = new ImageData(SwtLauncher.class
				.getResourceAsStream("/res/tray-on.png"));

		Image image = new Image(display, imageData);

		final Tray tray = display.getSystemTray();

		if (tray == null) {

			System.out.println("can not get tray");

		} else {

			final TrayItem item = new TrayItem(tray, SWT.NONE);

			item.setToolTipText("PTP Local");

			item.addListener(SWT.Show, new Listener() {

				public void handleEvent(Event event) {

					System.out.println("show");

				}

			});

			item.addListener(SWT.Hide, new Listener() {

				public void handleEvent(Event event) {

					System.out.println("hide");

				}

			});

			item.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {

					System.out.println("selection");

				}

			});

			final Menu menu = new Menu(shell, SWT.POP_UP);

			for (int i = 0; i < 8; i++) {

				MenuItem mi = new MenuItem(menu, SWT.PUSH);

				mi.setText("Item" + i);

				mi.addListener(SWT.Selection, new Listener() {

					public void handleEvent(Event event) {

						System.out.println("selection " + event.widget);

					}

				});

				if (i == 0)
					menu.setDefaultItem(mi);

			}

			item.addListener(SWT.MenuDetect, new Listener() {

				public void handleEvent(Event event) {

					menu.setVisible(true);

				}

			});

			item.addListener(SWT.DefaultSelection, new Listener() {

				public void handleEvent(Event event) {

					startServer();

				}

			});

			item.setImage(image);
			item.setVisible(true);

		}

		shell.setBounds(0, 0, 0, 0);

		shell.open();
		shell.setVisible(false);

		while (!shell.isDisposed()) {

			if (!display.readAndDispatch())

				display.sleep();

		}

		image.dispose();

		display.dispose();

	}

}
