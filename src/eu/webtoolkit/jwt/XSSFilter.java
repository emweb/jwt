/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLEntityResolver;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParseException;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLUtil;


class XSSFilter implements IXMLBuilder, IXMLEntityResolver {
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

	private StringBuilder textBuffer = new StringBuilder();
	private int discarding = 0;
	private boolean emptyTag;

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

	public StringBuilder result() {
		return textBuffer;
	}

	static boolean removeScript(CharSequence text) {
		try {
			WString wText = WString.toWString(text);
			
			XSSFilter filter = new XSSFilter();
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			parser.setBuilder(filter);
			parser.setResolver(filter);
			IXMLReader reader
			= StdXMLReader.stringReader("<span>" + wText.getValue() + "</span>");
			parser.setReader(reader);
			parser.parse();

			StringBuilder filtered = filter.result();

			// 6 and 7 correct for respectively <span> and </span>
			wText.set(filtered.substring(6, filtered.length() - 7));

			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (discarding != 0)
			return;

		if (XSSUtils.isBadAttribute(key) || XSSUtils.isBadAttributeValue(key, value)) {
			WApplication.getInstance().log("warn").append("(XSS) discarding invalid attribute: " + key + ": " + value);
			return;
		}

		textBuffer.append(' ');
		textBuffer.append(key + "=\"" + value + "\"");
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (discarding != 0)
			return;

        char[] buf = new char[1024];
        for (;;) {
            int size = reader.read(buf);
            if (size < 0) {
                break;
            }
            textBuffer.append(buf, 0, size);
        }
		emptyTag = false;
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (discarding == 0)
			textBuffer.append(">");
	}

	public Object getResult() throws Exception {
		return null;
	}

	public void newProcessingInstruction(String target, Reader reader) throws Exception {
	}

	public void startBuilding(String systemID, int lineNr) throws Exception {
	}

	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		if (discarding == 0 && XSSUtils.isBadTag(name)) {
			discarding = 1;
			WApplication.getInstance().log("warn").append("(XSS) discarding invalid tag: " + name);
			return;
		}

		if (discarding != 0) {
			++discarding;
			return;
		}

		textBuffer.append('<');
		textBuffer.append(name);

		emptyTag = true;
	}

	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (discarding == 0) {
			if (emptyTag && DomElement.isSelfClosingTag(name)) {
				textBuffer.deleteCharAt(textBuffer.length() - 1);
				textBuffer.append("/>");
			} else {
				textBuffer.append("</");
				textBuffer.append(name);
				textBuffer.append('>');
			}
		} else {
			discarding--;
		}

		emptyTag = false;
	}
}