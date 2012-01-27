package eu.webtoolkit.jwt.auth;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;

class AuthUtils {
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
			e.printStackTrace();
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
}
