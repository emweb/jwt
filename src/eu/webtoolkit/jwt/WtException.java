package eu.webtoolkit.jwt;

public class WtException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public WtException(String msg) {
		super(msg);
	}

	public WtException(String msg, Exception wrapped) {
		super(msg, wrapped);
	}
}
