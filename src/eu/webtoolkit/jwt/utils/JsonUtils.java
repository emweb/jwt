package eu.webtoolkit.jwt.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class JsonUtils {
	public static boolean orIfNullBoolean(JsonElement e, boolean defaultValue) {
		if (e == null || e.isJsonNull())
			return defaultValue;
		else {
			JsonPrimitive primitive = e.getAsJsonPrimitive();
			if (primitive.isBoolean())
				return primitive.getAsBoolean();
			else 
				return Boolean.parseBoolean(primitive.getAsString());
		}
	}

	public static String orIfNullString(JsonElement e, String defaultValue) {
		if (e == null || e.isJsonNull())
			return defaultValue;
		else
			return e.getAsString();
	}

	public static int orIfNullInt(JsonElement e, int defaultValue) {
		if (e == null || e.isJsonNull())
			return defaultValue;
		else
			return e.getAsInt();
	}

	public static double orIfNullDouble(JsonElement e, double defaultValue) {
		if (e == null || e.isJsonNull())
			return defaultValue;
		else
			return e.getAsDouble();
	}

	public static JsonElement toNumber(JsonElement e) {
		if (e == null || e.isJsonNull())
			return e;
		if (e.isJsonPrimitive() && ((JsonPrimitive)e).isNumber())
			return e;
		return new JsonNull();
	}

	public static boolean isNull(JsonElement e) {
		return e == null || e.isJsonNull();
	}
}
