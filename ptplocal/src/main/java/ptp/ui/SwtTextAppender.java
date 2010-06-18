package ptp.ui;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.custom.StyledText;

import ptp.Config;

public class SwtTextAppender extends AppenderSkeleton {

	private StyledText textWidget;
	private int lines;

	public SwtTextAppender() {

		this.lines = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.gui.log.lines", "100"));

	}

	public void setTextWidget(StyledText textWidget) {
		this.textWidget = textWidget;
	}

	@Override
	protected void append(LoggingEvent event) {
		final LoggingEvent _event = event;
		textWidget.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				_append(_event);
			}

		});

	}

	private void _append(LoggingEvent event) {

		if (this.textWidget != null) {
			String text = this.layout.format(event);
			_appendText(text);

			if (layout.ignoresThrowable()) {
				String[] s = event.getThrowableStrRep();
				if (s != null) {
					int len = s.length;
					for (int i = 0; i < len; i++) {
						_appendText(s[i]);
						_appendText(Layout.LINE_SEP);
					}
				}
			}

			this.textWidget.setSelection(this.textWidget.getCharCount());
		}
	}

	private void _appendText(String text) {
		if (this.textWidget != null) {
			int overLines = this.textWidget.getLineCount() - this.lines - 1;

			if (overLines > 0) {
				int removeEnd = this.textWidget.getContent().getOffsetAtLine(
						overLines);

				this.textWidget.replaceTextRange(0, removeEnd, "");
			}

			this.textWidget.append(text);
		}
	}

	@Override
	public void close() {
		this.textWidget.setText("");

	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

}
