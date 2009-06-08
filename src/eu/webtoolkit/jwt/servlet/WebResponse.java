package eu.webtoolkit.jwt.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WebResponse extends HttpServletResponseWrapper {

	private OutputStreamWriter outWriter;
	private OutputStream outputStream = null;
	private HttpServletRequest request;
	private int id;

	public WebResponse(HttpServletResponse response, HttpServletRequest request) {
		super(response);

		this.request = request;

		try {
			outWriter = new OutputStreamWriter(getOutputStream(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public WebResponse(OutputStream out) {
		super(null);

		this.outputStream = out;

		try {
			outWriter = new OutputStreamWriter(getOutputStream(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Writer out() {
		return this.outWriter;
	}

	public void setRedirect(String url) {
		try {
			sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setId(int i) {
		id = i;
	}

	public int getId() {
		return id;
	}

	public void flush() {
		try {
			outWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPathInfo() {
		String result = request.getPathInfo();
		return result == null ? "" : result;
	}

}
