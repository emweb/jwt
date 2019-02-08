package eu.webtoolkit.jwt.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpMessage {
	private static final Logger logger = LoggerFactory.getLogger(HttpMessage.class);
	
	HttpMessage() {
		headers_ = new ArrayList<Header>();
		status_ = -1;
	}

	HttpMessage(List<Header> headers) {
		headers_ = headers;
		status_ = -1;
	}
	
	HttpMessage(HttpResponse response) {
		headers_ = new ArrayList<Header>();
		for (Header h : response.getAllHeaders())
			headers_.add(h);
		status_ = response.getStatusLine().getStatusCode();
		
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line; 
			while ((line = rd.readLine()) != null) { 
				addBodyText(line);
			}
		} catch (IllegalStateException e) {
			logger.info("HttpMessage(HttpResponse): illegal state", e);
		} catch (IOException e) {
			logger.info("HttpMessage(HttpResponse): IOException", e);
		}
	}

	void setStatus(int status) {
		status_ = status;
	}

	int getStatus() {
		return status_;
	}

	void setHeader(String name, String value) {
		for (int i = 0; i < headers_.size(); ++i) {
			if (headers_.get(i).getName().equals(name)) {
				headers_.set(i, new BasicHeader(name, value));
				return;
			}
		}

		addHeader(name, value);
	}

	void addHeader(String name, String value) {
		headers_.add(new BasicHeader(name, value));
	}

	List<Header> getHeaders() {
		return headers_;
	}

	String getHeader(String name) {
		for (Header h : headers_) {
			if (h.getName().equals(name))
				return h.getValue();
		}
		return null;
	}

	void addBodyText(String text) {
		body_.append(text);
	}

	String getBody() {
		return body_.toString();
	}

	private int status_;
	private List<Header> headers_;
	private StringBuffer body_ = new StringBuffer();
}
