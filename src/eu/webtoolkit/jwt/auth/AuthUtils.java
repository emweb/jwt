package eu.webtoolkit.jwt.auth;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.Utils;
import eu.webtoolkit.jwt.WException;
import eu.webtoolkit.jwt.WtServlet;

class AuthUtils {
	private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
	
	static void parseFormUrlEncoded(HttpMessage response, Map<String, String[]> parameters) {
		try {
			StringEntity entity = new StringEntity(response.getBody(), null);
			entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
			List<NameValuePair> valuePairs = URLEncodedUtils.parse(entity);
			for (NameValuePair nvp : valuePairs) {
				String [] values = new String[1];
				values[0] = nvp.getValue();
				parameters.put(nvp.getName(), values);
			}
		} catch (IOException e) {
			logger.info("parseFormUrlEncoded: ignoring exception", e);
		}
	}

	static String getParamValue(Map<String, String[]> params, String name) {
		String[] values = params.get(name);
		if (values != null && values.length > 0) 
			return values[0];
		else
			return null;
	}
	
	static String createSalt(int length) {
		Random r = new SecureRandom();
		byte[] salt = new byte[length];
		r.nextBytes(salt);
		return new String(salt);
	}

	static String encodeState(final String secret, final String url) {
		String hash = Utils.base64Encode(Utils.hmac_sha1(url, secret));
		String b = Utils.base64Encode(hash + "|" + url, false);
		b = StringUtils.replace(b, "+", "-");
		b = StringUtils.replace(b, "/", "_");
		b = StringUtils.replace(b, "=", ".");
		return b;
	}

	static String decodeState(final String secret, final String state) {
		String s = state;
		s = StringUtils.replace(s, "-", "+");
		s = StringUtils.replace(s, "_", "/");
		s = StringUtils.replace(s, ".", "=");
		s = Utils.base64DecodeS(s);
		int i = s.indexOf('|');
		if (i != -1) {
			String url = s.substring(i + 1);
			String check = encodeState(secret, url);
			if (check.equals(state)) {
				return url;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	static String configurationProperty(final String prefix, final String property) {
		WtServlet instance = WtServlet.getInstance();
		if (instance != null) {
			String result = "";
			boolean error;
			String v = instance.readConfigurationProperty(property, result);
			if (v != result) {
				error = false;
				result = v;
			} else {
				error = true;
			}
			if (error) {
				throw new WException(prefix + ": no '" + property + "' property configured");
			}
			return result;
		} else {
			throw new WException(prefix + ": could not find a WtServlet instance");
		}
	}
}
