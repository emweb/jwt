package eu.webtoolkit.jwt.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.Signal2;
import eu.webtoolkit.jwt.WObject;

class HttpClient {
	private static Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	private org.apache.http.impl.client.DefaultHttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
	
	private Signal2<Exception, HttpMessage> done = new Signal2<Exception, HttpMessage>();
	
	public HttpClient(WObject object) {
		
	}

	public void get(String url) {
		HttpGet get = new HttpGet(url);
		get(get);
	}
	
	public void get(String url, List<Header> headers) {
		HttpGet get = new HttpGet(url);
		Header[] headersArray = new Header[headers.size()];
		headers.toArray(headersArray);
		get.setHeaders(headersArray);
		get(get);
	}
	
	private void get(HttpGet get) {
		try {
			HttpResponse response = httpClient.execute(get);
			
			done.trigger(null, new HttpMessage(response));
		} catch (ClientProtocolException e) {
			logger.info("Exception in get({})", get.getURI().toString(), e);
			done.trigger(e, null);
		} catch (IOException e) {
			logger.info("Exception in get({})", get.getURI().toString(), e);
			done.trigger(e, null);
		}
	}
	
	public void post(String url, HttpMessage message) {
		HttpPost post = new HttpPost(url);
		try {
			for (Header h : message.getHeaders())
				post.addHeader(h);
			post.setEntity(new StringEntity(message.getBody()));

			HttpResponse answer = httpClient.execute(post);
			done.trigger(null, new HttpMessage(answer));
		} catch (ClientProtocolException e) {
			logger.info("Exception in post({})", url, e);
			done.trigger(e, null);
		} catch (IOException e) {
			logger.info("Exception in post({})", url, e);
			done.trigger(e, null);
		}
	}

	public void setTimeout(int timeout) {
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
	}

	public void setMaximumResponseSize(int i) {

	}
	
	public Signal2<Exception, HttpMessage> done() {
		return done;
	}

	public static boolean parseUrl(String urlString, URL parsedUrl) {
		try {
			java.net.URL url = new java.net.URL(urlString);
			
			parsedUrl.host = url.getHost();
			parsedUrl.path = url.getPath();
			parsedUrl.port = url.getPort();
			parsedUrl.protocol = url.getProtocol();
			
			return true;
		} catch (MalformedURLException e) {
			logger.error("Illegally formed URL: " + urlString);
			return false;
		}
	}
}
