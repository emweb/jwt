package eu.webtoolkit.jwt;

public class WLogEntry {

	private StringBuilder buffer;

	public WLogEntry() {
		this.buffer = new StringBuilder();
	}

	public WLogEntry append(String s) {
		buffer.append(s);

		return this;
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	public WLogEntry append(char c) {
		buffer.append(c);

		return this;
	}

	public WLogEntry append(int row) {
		buffer.append(row);

		return this;
	}

}
