package eu.webtoolkit.jwt;

import java.io.IOException;


class UriUtils {
	static boolean isDataUri(String uriString) {
		return uriString.startsWith("data:");
	}

	static Uri parseDataUri(String uriString) {
		Uri uri = new Uri();
		int dataEndPos = uriString.indexOf("data:") + 5;
		int commaPos = uriString.indexOf(",");
		if (commaPos == -1) {
			commaPos = dataEndPos;
		}

		uri.mimeType = uriString.substring(dataEndPos, dataEndPos + commaPos
				- dataEndPos);
		String data = uriString.substring(commaPos + 1);
		try {
			uri.data = Utils.base64Decode(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!uri.mimeType.endsWith(";base64") || uri.data.length == 0) {
			throw new WException("Ill formed data URI: " + uriString);
		} else {
			uri.mimeType = uri.mimeType.substring(0, 0 + uri.mimeType.indexOf(";"));
			return uri;
		}
	}
}
