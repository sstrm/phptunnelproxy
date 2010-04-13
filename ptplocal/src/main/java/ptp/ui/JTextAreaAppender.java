package ptp.ui;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class JTextAreaAppender extends AppenderSkeleton {

	protected JTextArea textArea;
	protected int entries;
	protected int maxEntries;

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
		this(null);
	}

	public JTextAreaAppender(JTextArea textArea) {
		this(textArea, 1);
	}

	public JTextAreaAppender(JTextArea textArea, int maxEntries) {

		this.entries = 0;
		this.maxEntries = maxEntries;

		setTextArea(textArea);
	}

	public JTextArea getTextArea() {
		return this.textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public int getMaxEntries() {
		return this.maxEntries;
	}

	/**
	 * Sets the maximum number of logging entries.
	 * 
	 * @param value
	 *            - maximum number of logging entries. This value is ignored if
	 *            the component supports just 1 line.
	 */
	public void setMaxEntries(int value) {
		if (this.entries > value) {
			// the new maxEntry value is smaller than the actual entry counter
			// we have to delete the oldest entries
			int toomuch = this.entries - value;

			try {
				Document doc = textArea.getDocument();
				int endOfs = textArea.getLineEndOffset(toomuch - 1);
				int docLen = doc.getLength();
				// String docText = textArea.getText();
				if (docLen < endOfs)
					doc.remove(0, docLen);
				else
					doc.remove(0, endOfs);
				textArea.setCaretPosition(doc.getLength());
			} catch (Exception x) {
			}

			this.entries = value;
		}
		this.maxEntries = value;
	}

	public boolean requiresLayout() {
		return true;
	}

	public void append(LoggingEvent event) {
		String text = this.layout.format(event);
		try {
			Document doc = textArea.getDocument();
			if (entries == maxEntries) {
				// Delete 1 line
				int endOfs = textArea.getLineEndOffset(0);
				int docLen = doc.getLength();
				// String docText = textArea.getText();
				if (docLen < endOfs)
					doc.remove(0, docLen);
				else
					doc.remove(0, endOfs);
				entries -= 1;
			}
			textArea.append(text);
			if (entries == 0)
				doc.remove(1, 1);
			textArea.setCaretPosition(doc.getLength());
		} catch (Exception x) {
		}

		entries += 1;

	}

	/**
	 * Removes all logging entries from the UI elements
	 * 
	 */
	public void reset() {
		textArea.setText("");
		this.entries = 0;
	}

	public void close() {
		reset();
	}
}
