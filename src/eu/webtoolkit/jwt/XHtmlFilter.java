package eu.webtoolkit.jwt;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLEntityResolver;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.XMLParseException;
import net.n3.nanoxml.XMLUtil;

class XHtmlFilter implements IXMLBuilder, IXMLEntityResolver {
	static {
		XMLUtil.convertNumericalCharacterEntities = false;
	}
  
	static Set<String> xhtmlEntities = new HashSet<String> (Arrays.asList(new String[] { 
				"nbsp",
				"iexcl",
				"cent",
				"pound",
				"curren",
				"yen",
				"brvbar",
				"sect",
				"uml",
				"copy",
				"ordf",
				"laquo",
				"not",
				"shy",
				"reg",
				"macr",
				"deg",
				"plusmn",
				"sup2",
				"sup3",
				"acute",
				"micro",
				"para",
				"middot",
				"cedil",
				"sup1",
				"ordm",
				"raquo",
				"frac14",
				"frac12",
				"frac34",
				"iquest",
				"Agrave",
				"Aacute",
				"Acirc",
				"Atilde",
				"Auml",
				"Aring",
				"AElig",
				"Ccedil",
				"Egrave",
				"Eacute",
				"Ecirc",
				"Euml",
				"Igrave",
				"Iacute",
				"Icirc",
				"Iuml",
				"ETH",
				"Ntilde",
				"Ograve",
				"Oacute",
				"Ocirc",
				"Otilde",
				"Ouml",
				"times",
				"Oslash",
				"Ugrave",
				"Uacute",
				"Ucirc",
				"Uuml",
				"Yacute",
				"THORN",
				"szlig",
				"agrave",
				"aacute",
				"acirc",
				"atilde",
				"auml",
				"aring",
				"aelig",
				"ccedil",
				"egrave",
				"eacute",
				"ecirc",
				"euml",
				"igrave",
				"iacute",
				"icirc",
				"iuml",
				"eth",
				"ntilde",
				"ograve",
				"oacute",
				"ocirc",
				"otilde",
				"ouml",
				"divide",
				"oslash",
				"ugrave",
				"uacute",
				"ucirc",
				"uuml",
				"yacute",
				"thorn",
				"yuml",
				"quot",
				"amp",
				"lt",
				"gt",
				"apos",
				"OElig",
				"oelig",
				"Scaron",
				"scaron",
				"Yuml",
				"circ",
				"tilde",
				"ensp",
				"emsp",
				"thinsp",
				"zwnj",
				"zwj",
				"lrm",
				"rlm",
				"ndash",
				"mdash",
				"lsquo",
				"rsquo",
				"sbquo",
				"ldquo",
				"rdquo",
				"bdquo",
				"dagger",
				"Dagger",
				"permil",
				"lsaquo",
				"rsaquo",
				"euro",
				"fnof",
				"Alpha",
				"Beta",
				"Gamma",
				"Delta",
				"Epsilon",
				"Zeta",
				"Eta",
				"Theta",
				"Iota",
				"Kappa",
				"Lambda",
				"Mu",
				"Nu",
				"Xi",
				"Omicron",
				"Pi",
				"Rho",
				"Sigma",
				"Tau",
				"Upsilon",
				"Phi",
				"Chi",
				"Psi",
				"Omega",
				"alpha",
				"beta",
				"gamma",
				"delta",
				"epsilon",
				"zeta",
				"eta",
				"theta",
				"iota",
				"kappa",
				"lambda",
				"mu",
				"nu",
				"xi",
				"omicron",
				"pi",
				"rho",
				"sigmaf",
				"sigma",
				"tau",
				"upsilon",
				"phi",
				"chi",
				"psi",
				"omega",
				"thetasym",
				"upsih",
				"piv",
				"bull",
				"hellip",
				"prime",
				"Prime",
				"oline",
				"frasl",
				"weierp",
				"image",
				"real",
				"trade",
				"alefsym",
				"larr",
				"uarr",
				"rarr",
				"darr",
				"harr",
				"crarr",
				"lArr",
				"uArr",
				"rArr",
				"dArr",
				"hArr",
				"forall",
				"part",
				"exist",
				"empty",
				"nabla",
				"isin",
				"notin",
				"ni",
				"prod",
				"sum",
				"minus",
				"lowast",
				"radic",
				"prop",
				"infin",
				"ang",
				"and",
				"or",
				"cap",
				"cup",
				"int",
				"there4",
				"sim",
				"cong",
				"asymp",
				"ne",
				"equiv",
				"le",
				"ge",
				"sub",
				"sup",
				"nsub",
				"sube",
				"supe",
				"oplus",
				"otimes",
				"perp",
				"sdot",
				"lceil",
				"rceil",
				"lfloor",
				"rfloor",
				"lang",
				"rang",
				"loz",
				"spades",
				"clubs",
				"hearts",
				"diams"
		}));
	
	protected EscapeOStream writer = new EscapeOStream();
	protected boolean tagOpen = false;

	public XHtmlFilter() {
		super();
	}

	public void addExternalEntity(String name, String publicID, String systemID) {
	}

	public void addInternalEntity(String name, String value) {
	}

	public Reader getEntity(IXMLReader xmlReader, String name) throws XMLParseException {
		if (xhtmlEntities.contains(name))
			return new StringReader("&" + name + ";");
		else
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

	        // writer.pushEscape(EscapeOStream.RuleSet.PlainText);
	        writer.append(new String(buf, 0, size));
	        // writer.popEscape();
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