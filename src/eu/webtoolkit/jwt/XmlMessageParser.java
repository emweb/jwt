package eu.webtoolkit.jwt;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.XMLParseException;

public class XmlMessageParser extends XHtmlFilter {
	private Map<String, String> keyValues = new HashMap<String, String>();
	
	private int level = 0;
	private String currentKey = null;
	
	private final static int MESSAGE_LEVEL = 2;
	private final static String KEY_STRING = "id";

	public XmlMessageParser() {
		super(false);
	}

	public void addExternalEntity(String name, String publicID, String systemID) {
	}

	public void addInternalEntity(String name, String value) {
	}

	public void newProcessingInstruction(String target, Reader reader) throws Exception {
	}

	public void startBuilding(String systemID, int lineNr) throws Exception {
	}

	@Override
	public Reader getEntity(IXMLReader xmlReader, String name)
			throws XMLParseException {
		return super.getEntity(xmlReader, name, level == MESSAGE_LEVEL);
	}

	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (level == MESSAGE_LEVEL && KEY_STRING.equals(key)) 
			currentKey = value;
		
		if (level > MESSAGE_LEVEL)
			super.addAttribute(key, nsPrefix, nsURI, value, type);
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (level >= MESSAGE_LEVEL)
			super.addPCData(reader, systemID, lineNr);
		
		if (currentKey != null && currentKey.equals("test"))
			System.err.println(writer.toString());
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (level > MESSAGE_LEVEL) 
			super.elementAttributesProcessed(name, nsPrefix, nsURI);
	}

	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		++level;

		if (level > MESSAGE_LEVEL) 
			super.startElement(name, nsPrefix, nsURI, systemID, lineNr);
	}

	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (level > MESSAGE_LEVEL) 
			super.endElement(name, nsPrefix, nsURI);
			
		if (level == MESSAGE_LEVEL) {
			keyValues.put(currentKey, writer.toString());
			writer.clear();
		}
		
		--level;
	}

	@Override
	public Object getResult() throws Exception {
		return null;
	}
	
	public Map<String, String> getKeyValues() {
		return keyValues;
	}
}
