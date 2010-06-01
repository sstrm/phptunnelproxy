package ptp.ui;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import ptp.Config;

public class JTextAreaAppender extends AppenderSkeleton {

	private JTextArea textArea;
	private int lines;

	public static Appender getAppender(String appenderName) {
		return getAppender(appenderName, null);
	}

	public static Appender getAppender(String appenderName, String categoryName) {
		Appender result = null;
		Logger testcat;
		if (categoryName != null) {
			testcat = Logger.getLogger(categoryName);
			if (testcat != null) {
				result = testcat.getAppender(appenderName);
			}
		}
		if (result == null) {
			testcat = Logger.getRootLogger();
			result = testcat.getAppender(appenderName);
		}
		return result;
	}

	public JTextAreaAppender() {
		this.lines = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.gui.log.lines", "100"));
	}

	public JTextArea getTextArea() {
		return this.textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public boolean requiresLayout() {
		return true;
	}

	public void append(LoggingEvent event) {
		String text = this.layout.format(event);
		appendText(text);

		if (layout.ignoresThrowable()) {
			String[] s = event.getThrowableStrRep();
			if (s != null) {
				int len = s.length;
				for (int i = 0; i < len; i++) {
					appendText(s[i]);
					appendText(Layout.LINE_SEP);
				}
			}
		}
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	private void appendText(String text) {
		textArea.append(text);
		int overLines = textArea.getLineCount() - this.lines;
		while (overLines > 0) {
			try {
				int endOfs = textArea.getLineEndOffset(0);
				int docLen = textArea.getDocument().getLength();
				if (docLen < endOfs)
					textArea.getDocument().remove(0, docLen);
				else
					textArea.getDocument().remove(0, endOfs);
				
				overLines--;
			} catch (BadLocationException e) {
			}
		}

	}

	/**
	 * Removes all logging entries from the UI elements
	 * 
	 */
	public void reset() {
		textArea.setText("");
	}

	public void close() {
		reset();
	}
}
