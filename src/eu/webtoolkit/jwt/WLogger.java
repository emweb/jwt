package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

class WLogger {

	private Writer writer;
	private WLogEntry lastEntry;

	public WLogger(OutputStream stream) {
		this.writer = new OutputStreamWriter(stream);
		this.lastEntry = null;
	}

	public WLogEntry getEntry() {
		if (lastEntry != null) {
			try {
				writer.append(lastEntry.toString()).append('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return lastEntry = new WLogEntry();
	}

}
