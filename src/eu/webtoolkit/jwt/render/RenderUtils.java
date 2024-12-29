package eu.webtoolkit.jwt.render;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;
import eu.webtoolkit.jwt.XHtmlFilter;

public class RenderUtils {
	private static final Logger logger = LoggerFactory.getLogger(RenderUtils.class);
	
	static boolean isXmlElement(XMLElement node) {
		return node.getName() != null;
	}

	static void fetchBlockChildren(XMLElement node, Block block, List<Block> children) {
		for (Object o : node.getChildren())
			if (o instanceof XMLElement)
				children.add(new Block((XMLElement)o, block));
	}
	
	static boolean normalizeWhitespace(Block block, XMLElement node, boolean haveWhitespace, XMLElement doc) {
		String v = node.getContent();
	      
		StringBuilder s = new StringBuilder(v.length());
	      
		for (int i = 0; i < v.length(); ++i) {
		  if (Block.isWhitespace(v.charAt(i))) {
		    if (!haveWhitespace)
		    	s.append(' ');
		    haveWhitespace = true;
		  } else {
		    s.append(v.charAt(i));
		    haveWhitespace = false;
		  }
		}
		
		node.setContent(s.toString());
		
		return haveWhitespace;
	}

	static String nodeValueToString(XMLElement node) {
		String result = node.getContent();

		if (result == null) {
			result = "";
			for (Object o : node.getChildren()) {
				XMLElement e = (XMLElement) o;
				result += e.getContent();
			}
		}
		
		return result;
	}

	private static void extractTextNodes(XMLElement e) {
		for (Object o : e.getChildren()) {
			XMLElement c = ((XMLElement)o);
			
			if (c.getChildren().size() > 0)
				extractTextNodes(c);
			
			if (c.getContent() != null && c.getName() != null) {
				IXMLElement textNode = c.createElement(null);
				textNode.setContent(c.getContent());
				c.setContent(null);
				c.addChild(textNode);
			}
		}
	}
	
//	private static void printXmlTree(XMLElement e, int level) {
//		for (Object o : e.getChildren()) {
//			XMLElement c = ((XMLElement)o);
//			for (int i = 0; i < level; ++i)
//				System.err.print("\t");
//			System.err.print(c.getName() + " : " + c.getContent());
//			System.err.print("\n");
//			
//			if (c.getChildren().size() > 0)
//				printXmlTree(c, level + 1);
//		}
//	}

	static XMLElement parseXHTML(String xhtml) {
		IXMLParser parser;
		try {
			xhtml = "<div>" + xhtml + "</div>";
			
			parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader = StdXMLReader.stringReader(xhtml); 
			parser.setReader(reader);
			parser.setResolver(new XHtmlFilter(true));
			XMLElement xml = (XMLElement) parser.parse();
			extractTextNodes(xml);
						
			return xml;
		} catch (ClassNotFoundException e) {
			logger.info("Exception while parsing xhtml", e);
			logger.trace("xhtml was: {}", xhtml);
		} catch (InstantiationException e) {
			logger.info("Exception while parsing xhtml", e);
			logger.trace("xhtml was: {}", xhtml);
		} catch (IllegalAccessException e) {
			logger.info("Exception while parsing xhtml", e);
			logger.trace("xhtml was: {}", xhtml);
		} catch (XMLException e) {
			logger.info("Exception while parsing xhtml: {}", e.toString(), e);
			logger.trace("xhtml was: {}", xhtml);
		}
		return null;
	}
}
