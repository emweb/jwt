package eu.webtoolkit.jwt;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLEntityResolver;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.XMLParseException;
import net.n3.nanoxml.XMLUtil;

public class XHtmlFilter implements IXMLBuilder, IXMLEntityResolver {
	protected static Map<String, Integer> xhtmlEntities = new HashMap<String, Integer>();
	protected EscapeOStream writer = new EscapeOStream();
	protected boolean tagOpen = false;
	private boolean defaultResolveToUnicode;
	
	static {
		xhtmlEntities.put("quot" , 34);
		xhtmlEntities.put("amp" , 38);
		xhtmlEntities.put("lt" , 60);
		xhtmlEntities.put("gt" , 62);
		xhtmlEntities.put("OElig" , 338);
		xhtmlEntities.put("oelig" , 339);
		xhtmlEntities.put("Scaron" , 352);
		xhtmlEntities.put("scaron" , 353);
		xhtmlEntities.put("Yuml" , 376);
		xhtmlEntities.put("circ" , 710);
		xhtmlEntities.put("tilde" , 732);
		xhtmlEntities.put("ensp" , 8194);
		xhtmlEntities.put("emsp" , 8195);
		xhtmlEntities.put("thinsp" , 8201);
		xhtmlEntities.put("zwnj" , 8204);
		xhtmlEntities.put("zwj" , 8205);
		xhtmlEntities.put("lrm" , 8206);
		xhtmlEntities.put("rlm" , 8207);
		xhtmlEntities.put("ndash" , 8211);
		xhtmlEntities.put("mdash" , 8212);
		xhtmlEntities.put("lsquo" , 8216);
		xhtmlEntities.put("rsquo" , 8217);
		xhtmlEntities.put("sbquo" , 8218);
		xhtmlEntities.put("ldquo" , 8220);
		xhtmlEntities.put("rdquo" , 8221);
		xhtmlEntities.put("bdquo" , 8222);
		xhtmlEntities.put("dagger" , 8224);
		xhtmlEntities.put("Dagger" , 8225);
		xhtmlEntities.put("permil" , 8240);
		xhtmlEntities.put("lsaquo" , 8249);
		xhtmlEntities.put("rsaquo" , 8250);
		xhtmlEntities.put("euro" , 8364);
		xhtmlEntities.put("fnof" , 402);
		xhtmlEntities.put("Alpha" , 913);
		xhtmlEntities.put("Beta" , 914);
		xhtmlEntities.put("Gamma" , 915);
		xhtmlEntities.put("Delta" , 916);
		xhtmlEntities.put("Epsilon" , 917);
		xhtmlEntities.put("Zeta" , 918);
		xhtmlEntities.put("Eta" , 919);
		xhtmlEntities.put("Theta" , 920);
		xhtmlEntities.put("Iota" , 921);
		xhtmlEntities.put("Kappa" , 922);
		xhtmlEntities.put("Lambda" , 923);
		xhtmlEntities.put("Mu" , 924);
		xhtmlEntities.put("Nu" , 925);
		xhtmlEntities.put("Xi" , 926);
		xhtmlEntities.put("Omicron" , 927);
		xhtmlEntities.put("Pi" , 928);
		xhtmlEntities.put("Rho" , 929);
		xhtmlEntities.put("Sigma" , 931);
		xhtmlEntities.put("Tau" , 932);
		xhtmlEntities.put("Upsilon" , 933);
		xhtmlEntities.put("Phi" , 934);
		xhtmlEntities.put("Chi" , 935);
		xhtmlEntities.put("Psi" , 936);
		xhtmlEntities.put("Omega" , 937);
		xhtmlEntities.put("alpha" , 945);
		xhtmlEntities.put("beta" , 946);
		xhtmlEntities.put("gamma" , 947);
		xhtmlEntities.put("delta" , 948);
		xhtmlEntities.put("epsilon" , 949);
		xhtmlEntities.put("zeta" , 950);
		xhtmlEntities.put("eta" , 951);
		xhtmlEntities.put("theta" , 952);
		xhtmlEntities.put("iota" , 953);
		xhtmlEntities.put("kappa" , 954);
		xhtmlEntities.put("lambda" , 955);
		xhtmlEntities.put("mu" , 956);
		xhtmlEntities.put("nu" , 957);
		xhtmlEntities.put("xi" , 958);
		xhtmlEntities.put("omicron" , 959);
		xhtmlEntities.put("pi" , 960);
		xhtmlEntities.put("rho" , 961);
		xhtmlEntities.put("sigmaf" , 962);
		xhtmlEntities.put("sigma" , 963);
		xhtmlEntities.put("tau" , 964);
		xhtmlEntities.put("upsilon" , 965);
		xhtmlEntities.put("phi" , 966);
		xhtmlEntities.put("chi" , 967);
		xhtmlEntities.put("psi" , 968);
		xhtmlEntities.put("omega" , 969);
		xhtmlEntities.put("thetasym" , 977);
		xhtmlEntities.put("upsih" , 978);
		xhtmlEntities.put("piv" , 982);
		xhtmlEntities.put("bull" , 8226);
		xhtmlEntities.put("hellip" , 8230);
		xhtmlEntities.put("prime" , 8242);
		xhtmlEntities.put("Prime" , 8243);
		xhtmlEntities.put("oline" , 8254);
		xhtmlEntities.put("frasl" , 8260);
		xhtmlEntities.put("weierp" , 8472);
		xhtmlEntities.put("image" , 8465);
		xhtmlEntities.put("real" , 8476);
		xhtmlEntities.put("trade" , 8482);
		xhtmlEntities.put("alefsym" , 8501);
		xhtmlEntities.put("larr" , 8592);
		xhtmlEntities.put("uarr" , 8593);
		xhtmlEntities.put("rarr" , 8594);
		xhtmlEntities.put("darr" , 8595);
		xhtmlEntities.put("harr" , 8596);
		xhtmlEntities.put("crarr" , 8629);
		xhtmlEntities.put("lArr" , 8656);
		xhtmlEntities.put("uArr" , 8657);
		xhtmlEntities.put("rArr" , 8658);
		xhtmlEntities.put("dArr" , 8659);
		xhtmlEntities.put("hArr" , 8660);
		xhtmlEntities.put("forall" , 8704);
		xhtmlEntities.put("part" , 8706);
		xhtmlEntities.put("exist" , 8707);
		xhtmlEntities.put("empty" , 8709);
		xhtmlEntities.put("nabla" , 8711);
		xhtmlEntities.put("isin" , 8712);
		xhtmlEntities.put("notin" , 8713);
		xhtmlEntities.put("ni" , 8715);
		xhtmlEntities.put("prod" , 8719);
		xhtmlEntities.put("sum" , 8721);
		xhtmlEntities.put("minus" , 8722);
		xhtmlEntities.put("lowast" , 8727);
		xhtmlEntities.put("radic" , 8730);
		xhtmlEntities.put("prop" , 8733);
		xhtmlEntities.put("infin" , 8734);
		xhtmlEntities.put("ang" , 8736);
		xhtmlEntities.put("and" , 8743);
		xhtmlEntities.put("or" , 8744);
		xhtmlEntities.put("cap" , 8745);
		xhtmlEntities.put("cup" , 8746);
		xhtmlEntities.put("int" , 8747);
		xhtmlEntities.put("there4" , 8756);
		xhtmlEntities.put("sim" , 8764);
		xhtmlEntities.put("cong" , 8773);
		xhtmlEntities.put("asymp" , 8776);
		xhtmlEntities.put("ne" , 8800);
		xhtmlEntities.put("equiv" , 8801);
		xhtmlEntities.put("le" , 8804);
		xhtmlEntities.put("ge" , 8805);
		xhtmlEntities.put("sub" , 8834);
		xhtmlEntities.put("sup" , 8835);
		xhtmlEntities.put("nsub" , 8836);
		xhtmlEntities.put("sube" , 8838);
		xhtmlEntities.put("supe" , 8839);
		xhtmlEntities.put("oplus" , 8853);
		xhtmlEntities.put("otimes" , 8855);
		xhtmlEntities.put("perp" , 8869);
		xhtmlEntities.put("sdot" , 8901);
		xhtmlEntities.put("lceil" , 8968);
		xhtmlEntities.put("rceil" , 8969);
		xhtmlEntities.put("lfloor" , 8970);
		xhtmlEntities.put("rfloor" , 8971);
		xhtmlEntities.put("lang" , 9001);
		xhtmlEntities.put("rang" , 9002);
		xhtmlEntities.put("loz" , 9674);
		xhtmlEntities.put("spades" , 9824);
		xhtmlEntities.put("clubs" , 9827);
		xhtmlEntities.put("hearts" , 9829);
		xhtmlEntities.put("diams" , 9830);
		xhtmlEntities.put("nbsp" , 160);
		xhtmlEntities.put("iexcl" , 161);
		xhtmlEntities.put("cent" , 162);
		xhtmlEntities.put("pound" , 163);
		xhtmlEntities.put("curren" , 164);
		xhtmlEntities.put("yen" , 165);
		xhtmlEntities.put("brvbar" , 166);
		xhtmlEntities.put("sect" , 167);
		xhtmlEntities.put("uml" , 168);
		xhtmlEntities.put("copy" , 169);
		xhtmlEntities.put("ordf" , 170);
		xhtmlEntities.put("laquo" , 171);
		xhtmlEntities.put("not" , 172);
		xhtmlEntities.put("shy" , 173);
		xhtmlEntities.put("reg" , 174);
		xhtmlEntities.put("macr" , 175);
		xhtmlEntities.put("deg" , 176);
		xhtmlEntities.put("plusmn" , 177);
		xhtmlEntities.put("sup2" , 178);
		xhtmlEntities.put("sup3" , 179);
		xhtmlEntities.put("acute" , 180);
		xhtmlEntities.put("micro" , 181);
		xhtmlEntities.put("para" , 182);
		xhtmlEntities.put("middot" , 183);
		xhtmlEntities.put("cedil" , 184);
		xhtmlEntities.put("sup1" , 185);
		xhtmlEntities.put("ordm" , 186);
		xhtmlEntities.put("raquo" , 187);
		xhtmlEntities.put("frac14" , 188);
		xhtmlEntities.put("frac12" , 189);
		xhtmlEntities.put("frac34" , 190);
		xhtmlEntities.put("iquest" , 191);
		xhtmlEntities.put("Agrave" , 192);
		xhtmlEntities.put("Aacute" , 193);
		xhtmlEntities.put("Acirc" , 194);
		xhtmlEntities.put("Atilde" , 195);
		xhtmlEntities.put("Auml" , 196);
		xhtmlEntities.put("Aring" , 197);
		xhtmlEntities.put("AElig" , 198);
		xhtmlEntities.put("Ccedil" , 199);
		xhtmlEntities.put("Egrave" , 200);
		xhtmlEntities.put("Eacute" , 201);
		xhtmlEntities.put("Ecirc" , 202);
		xhtmlEntities.put("Euml" , 203);
		xhtmlEntities.put("Igrave" , 204);
		xhtmlEntities.put("Iacute" , 205);
		xhtmlEntities.put("Icirc" , 206);
		xhtmlEntities.put("Iuml" , 207);
		xhtmlEntities.put("ETH" , 208);
		xhtmlEntities.put("Ntilde" , 209);
		xhtmlEntities.put("Ograve" , 210);
		xhtmlEntities.put("Oacute" , 211);
		xhtmlEntities.put("Ocirc" , 212);
		xhtmlEntities.put("Otilde" , 213);
		xhtmlEntities.put("Ouml" , 214);
		xhtmlEntities.put("times" , 215);
		xhtmlEntities.put("Oslash" , 216);
		xhtmlEntities.put("Ugrave" , 217);
		xhtmlEntities.put("Uacute" , 218);
		xhtmlEntities.put("Ucirc" , 219);
		xhtmlEntities.put("Uuml" , 220);
		xhtmlEntities.put("Yacute" , 221);
		xhtmlEntities.put("THORN" , 222);
		xhtmlEntities.put("szlig" , 223);
		xhtmlEntities.put("agrave" , 224);
		xhtmlEntities.put("aacute" , 225);
		xhtmlEntities.put("acirc" , 226);
		xhtmlEntities.put("atilde" , 227);
		xhtmlEntities.put("auml" , 228);
		xhtmlEntities.put("aring" , 229);
		xhtmlEntities.put("aelig" , 230);
		xhtmlEntities.put("ccedil" , 231);
		xhtmlEntities.put("egrave" , 232);
		xhtmlEntities.put("eacute" , 233);
		xhtmlEntities.put("ecirc" , 234);
		xhtmlEntities.put("euml" , 235);
		xhtmlEntities.put("igrave" , 236);
		xhtmlEntities.put("iacute" , 237);
		xhtmlEntities.put("icirc" , 238);
		xhtmlEntities.put("iuml" , 239);
		xhtmlEntities.put("eth" , 240);
		xhtmlEntities.put("ntilde" , 241);
		xhtmlEntities.put("ograve" , 242);
		xhtmlEntities.put("oacute" , 243);
		xhtmlEntities.put("ocirc" , 244);
		xhtmlEntities.put("otilde" , 245);
		xhtmlEntities.put("ouml" , 246);
		xhtmlEntities.put("divide" , 247);
		xhtmlEntities.put("oslash" , 248);
		xhtmlEntities.put("ugrave" , 249);
		xhtmlEntities.put("uacute" , 250);
		xhtmlEntities.put("ucirc" , 251);
		xhtmlEntities.put("uuml" , 252);
		xhtmlEntities.put("yacute" , 253);
		xhtmlEntities.put("thorn" , 254);
		xhtmlEntities.put("yuml" , 255);
	}
	
	public XHtmlFilter(boolean resolveToUnicode) {
		super();
		this.defaultResolveToUnicode = resolveToUnicode;
	}

	public void addExternalEntity(String name, String publicID, String systemID) {
	}

	public void addInternalEntity(String name, String value) {
	}

	public Reader getEntity(IXMLReader xmlReader, String name) throws XMLParseException {
		return getEntity(xmlReader, name, defaultResolveToUnicode);
	}
	
	public Reader getEntity(IXMLReader xmlReader, String name, boolean resolveToUnicode) {
		Integer unicodeSymbol = xhtmlEntities.get(name);
		if (unicodeSymbol != null) {
			if (!resolveToUnicode) {
				return new StringReader("&" + name + ";");
			} else {
				return new StringReader((char)unicodeSymbol.intValue() + "");
			}
		} else
			return null;
	}

	public boolean isExternalEntity(String name) {
		return false;
	}

	public String result() {
		writer.flush();
		return writer.toString();
	}

	public void newProcessingInstruction(String target, Reader reader) throws Exception {
	}

	public void startBuilding(String systemID, int lineNr) throws Exception {
	}

	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		writer.append(' ' + key + "=\"");
		writer.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
		writer.append(value);
		writer.popEscape();
		writer.append('"');
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
	    char[] buf = new char[1024];
	    for (;;) {
	        int size = reader.read(buf);
	        if (size < 0) {
	            break;
	        }

	        if (tagOpen) {
	        	writer.append('>');
	        	tagOpen = false;
	        }

	        writer.append(new String(buf, 0, size));
	    }
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		tagOpen = true;
	}

	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		if (tagOpen) {
			writer.append('>');
			tagOpen = false;
		}

		writer.append('<');
		if (nsPrefix != null) 
			writer.append(nsPrefix + ":");
		writer.append(name);
	}

	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (tagOpen && DomElement.isSelfClosingTag(name)) {
			writer.append("/>");
		} else {
			if (tagOpen)
				writer.append('>');
			writer.append("</");
			if (nsPrefix != null)
				writer.append(nsPrefix + ":");
			writer.append(name);
			writer.append('>');
		}

		tagOpen = false;
	}

	@Override
	public Object getResult() throws Exception {
		return null;
	}
}