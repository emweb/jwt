package eu.webtoolkit.jwt;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public class Utils {
	/** Computes an MD5 hash.
	 *
	 * This utility function computes an MD5 hash, and returns the hash value.
	 */
	public static byte[] md5(String msg) {
		try {
			MessageDigest d = MessageDigest.getInstance("MD5");
			return d.digest(msg.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
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
	
	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(String s) {
		return base64Encode(s.getBytes());
	}
	
	/** Performs Base64-encoding of data.
	 */
	public static String base64Encode(byte[] bytes) {
		return Base64.encodeBytes(bytes);
	}
	
	/** Performs Base64-decoding of data.
	 * 
	 * @throws IOException 
	 */
	public static byte[] base64Decode(String s) throws IOException {
		return base64Decode(s.getBytes());
	}
	
	/** Performs Base64-decoding of data.
	 * 
	 * @throws IOException 
	 */
	public static byte[] base64Decode(byte[] bytes) throws IOException {
		return Base64.decode(bytes);
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
	  String result = text;
	  WWebWidget.escapeText(result, flags.contains(HtmlEncodingFlag.EncodeNewLines) ? true : false);
	  return result;
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
}
