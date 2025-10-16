package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Utils {
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	/** Computes an MD5 hash.
	 *
	 * This utility function computes an MD5 hash, and returns the hash value.
	 */
	public static byte[] md5(String msg) {
		try {
			MessageDigest d = MessageDigest.getInstance("MD5");
			return d.digest(msg.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException", e);
		}
		return null;
	}

	public static byte[] sha1(String input) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			return mDigest.digest(input.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException", e);
	  	} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException", e);
		}
	  	return null;
	}


	/** Performs url encoding (aka percentage encoding).
	 *
	 * This utility function percent encodes a text so that it can be
	 * embodied verbatim in a URL (e.g. as a fragment).
	 *
	 * @see #urlDecode(String scope)
	 */
	public static String urlEncode(String scope) {
		return DomElement.urlEncodeS(scope);
	}

	/** Performs url decoding.
	 *
	 * This utility function percent encodes a text so that it can be
	 * embodied verbatim in a URL (e.g. as a fragment).
	 *
	 * @see #urlEncode(String scope)
	 */
	public static String urlDecode(String scope) {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < scope.length(); ++i) {
			char c = scope.charAt(i);

			if (c == '+') {
				result.append(' ');
			} else if (c == '%' && i + 2 < scope.length()) {
				int start = i + 1;
				String h = scope.substring(start, start + 2);
				try {
					long hval = Long.parseLong(h, 16);
					result.append("" + (byte) hval);
				} catch (NumberFormatException nfe) {
					result.append(c);
				}
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public static void parseFormUrlEncoded(String s, Map<String, String[]> parameters) {
		for (int pos = 0; pos < s.length();) {
			int nextAmp = s.indexOf("&", pos);
			int nextEq = s.indexOf("=", pos);
			int next = nextAmp != -1 && nextAmp < nextEq ? nextAmp : nextEq;
			if (next == -1 || s.charAt(next) == '&') {
				if (next == -1)
					next = s.length();
				String key = urlDecode(s.substring(pos, next));
				if (parameters.containsKey(key)) {
					String[] oldValues = parameters.get(key);
					String[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
					newValues[oldValues.length] = "";
					parameters.put(key, newValues);
				} else {
					parameters.put(key, new String[] {""});
				}
				pos = next + 1;
			} else {
				nextAmp = s.indexOf("&", next + 1);
				if (nextAmp == -1)
					nextAmp = s.length();

				String key = urlDecode(s.substring(pos, next));
				String value = urlDecode(s.substring(next + 1, nextAmp));

				if (parameters.containsKey(key)) {
					String[] oldValues = parameters.get(key);
					String[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
					newValues[oldValues.length] = value;
					parameters.put(key, newValues);
				} else {
					parameters.put(key, new String[] {value});
				}
				pos = nextAmp + 1;
			}
		}
	}


	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(String s) {
		return base64Encode(s, true);
	}

	public static String base64Encode(String s, boolean crlf) {
		try {
			return base64Encode(s.getBytes("UTF-8"), crlf);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException", e);
		}
		return null;
	}

	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.encodeBytes(bytes);
	}
	public static String base64Encode(byte[] bytes, boolean crlf) {
		return Base64.encodeBytes(bytes, crlf);
	}

	/** Performs Base64-decoding of data.
	 *
	 * @throws IOException
	 */
	public static byte[] base64Decode(String s) throws IOException {
		return Base64.decode(s.getBytes("UTF-8"));
	}

	public static String base64DecodeS(String s) {
	  try {
		return new String(Base64.decode(s.getBytes("US-ASCII")), "US-ASCII");
	  } catch(IOException e) {
		return "";
	  }
	}

  /** Performs Base32-encoding of data.
   */
  public static String base32Encode(String s) {
    return base32Encode(s, true);
  }

  public static String base32Encode(String s, boolean crlf) {
    try {
      return base32Encode(s.getBytes("UTF-8"), crlf);
    } catch (UnsupportedEncodingException e) {
      logger.error("UnsupportedEncodingException", e);
    }
    return null;
	}

  /** Performs Base32-encoding of data.
   */
  public static String base32Encode(byte[] bytes) {
    Base32 base32 = new Base32();
    return base32.encodeAsString(bytes);
  }

  public static String base32Encode(byte[] bytes, boolean crlf) {
    Base32 base32 = new Base32(76);
    return base32.encodeAsString(bytes);
  }

  /** Performs Base32-decoding of data.
   *
   * @throws IOException
   */
  public static byte[] base32Decode(String s) throws IOException {
    Base32 base32 = new Base32();
    return base32.decode(s.getBytes("UTF-8"));
  }

  public static String base32DecodeS(String s) {
    try {
      Base32 base32 = new Base32();
      return new String(base32.decode(s.getBytes("US-ASCII")), "US-ASCII");
    } catch(IOException e) {
      return "";
    }
  }

	/** An enumeration for HTML encoding flags.
	 */
	public enum HtmlEncodingFlag
	{
	  /** Encode new-lines as line breaks (&lt;br&gt;)
	   */
	  EncodeNewLines
	}

	/** Performs HTML encoding of text.
	 *
	 * This utility function escapes characters so that the text can
	 * be embodied verbatim in a HTML text block.
	 */
	public static String htmlEncode(String text, EnumSet<HtmlEncodingFlag> flags)
	{
	  return WWebWidget.escapeText(text, flags.contains(HtmlEncodingFlag.EncodeNewLines) ? true : false);
	}

	/**
	 * Performs HTML encoding of text.
	 * <p>
	 * Calls {@link Utils#htmlEncode(String text, EnumSet flags)
	 * Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class))}
	 */
	public static String htmlEncode(String text) {
		return Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class));
	}

	/** Performs HTML encoding of text.
	 *
	 * This utility function escapes characters so that the text can
	 * be embodied verbatim in a HTML text block.
	 *
	 * By default, newlines are ignored. By passing the {@link HtmlEncodingFlag#EncodeNewLines}
	 * flag, these may be encoded as line breaks (&lt;br&gt;).
	 */
	public static String htmlEncode(WString text, EnumSet<HtmlEncodingFlag> flags)
	{
		return htmlEncode(text.toString(), flags);
	}

	/**
	 * Performs HTML encoding of text.
	 * <p>
	 * Calls {@link Utils#htmlEncode(WString text, EnumSet flags)
	 * Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class))}
	 */
	public static String htmlEncode(WString text) {
		return Utils.htmlEncode(text, EnumSet.noneOf(HtmlEncodingFlag.class));
	}

	/**
	 * Performs HTML encoding of text.
	 * <p>
	 * Calls {@link Utils#htmlEncode(WString text, EnumSet flags)
	 * Utils.htmlEncode(text, flags)}
	 */
	public static String htmlEncode(String value, HtmlEncodingFlag flag, HtmlEncodingFlag... flags) {
		return htmlEncode(value, EnumSet.of(flag, flags));
	}

	/**
	 * Escape the given text for inclusion in an HTML attribute
	 * <p>
	 * This utility function escapes characters so that the \p text can
	 * be used as the value of an HTML attribute between double quotes.
	 * <p>
	 * The double quotes are <strong>not</strong> included in the output.
	 * <p>
	 * Example usage:
	 * <p>
	 * <pre>{@code
	 * String attribute = "name=\"" + htmlAttributeValue(value) + "\"";
	 * }</pre>
	 */
	public static String htmlAttributeValue(String text) {
		StringBuilder sb = new StringBuilder();
		DomElement.htmlAttributeValue(sb, text);
		return sb.toString();
	}

	/** Remove tags/attributes from text that are not passive.
	 *
	 * This removes tags and attributes from XHTML-formatted text that do
	 * not simply display something but may trigger scripting, and could
	 * have been injected by a malicious user for Cross-Site Scripting
	 * (XSS).
	 *
	 * This method is used by the library to sanitize XHTML-formatted text
	 * set in {@link WText}, but it may also be useful outside the library to
	 * sanitize user content when directly using JavaScript.
	 *
	 * Modifies the text if needed. When the text is not proper XML,
	 * returns false.
	 */
	public static boolean removeScript(CharSequence text) {
		return WWebWidget.removeScript(text);
	}

	static int memcmp(List<Byte> header, String string, int size) {
		for (int i = 0; i < size; i++) {
			if (header.get(i) != (byte) string.charAt(i))
				return 1;
		}

		return 0;
	}

	private static boolean isWhiteSpace(char c, String whiteSpaces) {
		for (int i = 0; i < whiteSpaces.length(); i++) {
			if (c == whiteSpaces.charAt(i))
				return true;
		}
		return false;
	}

	public static String strip(String s, String whiteSpaces) {
		int start = -1;
		int end = -1;

		for (int i = 0; i < s.length(); i++) {
			if (!isWhiteSpace(s.charAt(i), whiteSpaces)) {
				start = i;
				break;
			}
		}

		if (start == -1)
			return "";
		else
			s = s.substring(start);

		for (int i = s.length() - 1; i >= 0; i--) {
			if (!isWhiteSpace(s.charAt(i), whiteSpaces)) {
				end = i + 1;
				break;
			}
		}

		return s.substring(0, end);
	}

	public static void assignFontMatch(FontSupport.FontMatch fm1, FontSupport.FontMatch fm2) {
		fm1.setFileName(fm2.getFileName());
		fm1.setQuality(fm2.getQuality());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void copyList(List source, List destination) {
		destination.clear();
		for (Object o : source) {
			destination.add(o);
		}
	}

	public static void copyList(byte[] source, List<Byte> destination) {
		destination.clear();
		for (byte o : source) {
			destination.add(o);
		}
	}

	public static int hexToInt(String s) {
		return Integer.parseInt(s, 16);
	}

  /**
   * Converts the long value to a byte array containing the hexstring.
   * <p>
   * The hexstring of a long value is simple its hexadecimal representation
   * in string format. i.e. <strong>12</strong> would be <strong>C</strong>.
   * The string will always be then converted to be 16 bytes long.
   * <p>
   * The string is then converted to an array of bytes.
   * <p>
   */
  public static byte[] hexStrToBytes(long value) {
    String hex = Long.toHexString(value);
    while (hex.length() < 16) {
      hex = "0" + hex;
    }

    // Prepend 10 to the value, so that the BigInt is forces to contain
    // an actual 16 byte value. Otherwise, with leading 0's this may be
    // optimised to a smaller value. This way the resulting byte array is
    // always of the expected length (8).
    // This value "10" is later dropped when copying to the result array.
    byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
    byte[] ret = new byte[bArray.length - 1];

    for (int i = 0; i < ret.length; i++)
      ret[i] = bArray[i+1];
    return ret;
  }

	public static String createDataUrl(byte[] data, String mimeType) {
	  String url = "data:"+mimeType+";base64,";
	  String datab64 = base64Encode(data, false);
	  return url+datab64;
	}

	public static byte[] hmac(String msg, String key, String algo) {
        byte[] result = null;
		try {
			SecretKeySpec key_ = new SecretKeySpec((key).getBytes("UTF-8"), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key_);
			result = mac.doFinal(msg.getBytes("ASCII"));
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException", e);
		} catch (InvalidKeyException e) {
			logger.error("InvalidKeyException: {}", key, e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException", e);
		}
        return result;
	}

  public static String hmacWithEncoding(byte[] msg, byte[] key, String algo) {
    byte[] result = null;
    try {
      SecretKeySpec key_ = new SecretKeySpec(key, algo);
      Mac mac = Mac.getInstance(algo);
      mac.init(key_);
      result = mac.doFinal(msg);
    } catch (InvalidKeyException e) {
      logger.error("InvalidKeyException: {}", key, e);
    } catch (NoSuchAlgorithmException e) {
      logger.error("NoSuchAlgorithmException", e);
    }
    try {
      return new String(result, "ISO_8859_1");
    } catch (UnsupportedEncodingException e) {
      logger.error("NoSuchAlgorithmException", e);
      return "";
    }
  }

	public static byte[] hmac_sha1(String msg, String key) {
      return hmac(msg,key,"HmacSHA1");
  }

  public static String hmac_sha1WithEncoding(long msg, byte[] key) {
    return hmacWithEncoding(hexStrToBytes(msg),key,"HmacSHA1");
  }

	public static byte[] hmac_md5(String msg, String key) {
        return hmac(msg,key,"HmacMD5");
    }

  public static List<String> getWidgetStyleClasses(WWidget widget) {
    String styleClass = widget.getStyleClass();
    ArrayList<String> styleClassVec = new ArrayList<String>();

    StringUtils.split(styleClassVec, styleClass, " ", false);

    return styleClassVec;
    }
}
