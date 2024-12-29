package eu.webtoolkit.jwt;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.XMLParseException;

class XmlMessageParser extends XHtmlFilter {
	private Map<String, List<String>> keyValues = new HashMap<>();
	private int nplurals = 0;
	private String plural = "";
	
	private int level = 0;
	private String currentKey = null;
	private boolean currentMessageHasPlurals = false;
	private int currentPluralId = 0;

	private final static int MESSAGES_LEVEL = 1;
	private final static int MESSAGE_LEVEL = 2;
	private final static int PLURAL_LEVEL = 3;
	private final static String NPLURALS_STRING = "nplurals";
	private final static String PLURAL_STRING = "plural";
	private final static String KEY_STRING = "id";
	private final static String CASE_STRING = "case";

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
		return super.getEntity(xmlReader, name, false);
	}

	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (level == MESSAGES_LEVEL && NPLURALS_STRING.equals(key))
			nplurals = Integer.parseUnsignedInt(value);
		
		if (level == MESSAGES_LEVEL && PLURAL_STRING.equals(key)) {
			plural = htmlEntityDecode(value);
		}
			
		if (level == MESSAGE_LEVEL && KEY_STRING.equals(key)) 
			currentKey = value;
		
		if (level == PLURAL_LEVEL && currentMessageHasPlurals && CASE_STRING.equals(key))
			currentPluralId = Integer.parseUnsignedInt(value);
		
		if (!currentMessageHasPlurals && level > MESSAGE_LEVEL ||
			currentMessageHasPlurals && level > PLURAL_LEVEL)
			super.addAttribute(key, nsPrefix, nsURI, value, type);
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (!currentMessageHasPlurals && level >= MESSAGE_LEVEL ||
			currentMessageHasPlurals && level >= PLURAL_LEVEL)
			super.addPCData(reader, systemID, lineNr);
		
		if (currentKey != null && currentKey.equals("test"))
			System.err.println(writer.toString());
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (!currentMessageHasPlurals && level > MESSAGE_LEVEL ||
			currentMessageHasPlurals && level > PLURAL_LEVEL)
			super.elementAttributesProcessed(name, nsPrefix, nsURI);
	}

	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		++level;
		
		if (level == MESSAGE_LEVEL)
			currentMessageHasPlurals = false;
		
		if (level == PLURAL_LEVEL &&
			PLURAL_STRING.equals(name) &&
			!currentMessageHasPlurals) {
			writer.clear();
			currentMessageHasPlurals = true;
		}
		
		if (!currentMessageHasPlurals && level > MESSAGE_LEVEL ||
			currentMessageHasPlurals && level > PLURAL_LEVEL)
			super.startElement(name, nsPrefix, nsURI, systemID, lineNr);
	}

	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (!currentMessageHasPlurals && level > MESSAGE_LEVEL ||
			currentMessageHasPlurals && level > PLURAL_LEVEL)
			super.endElement(name, nsPrefix, nsURI);
		
		if (currentMessageHasPlurals && level == PLURAL_LEVEL) {
			List<String> values;
			if (!keyValues.containsKey(currentKey)) {
				values = new ArrayList<>();
				keyValues.put(currentKey, values);
				for (int i = 0; i < nplurals; ++i) {
					values.add("");
				}
			} else {
				values = keyValues.get(currentKey);
			}
			values.set(currentPluralId, writer.toString());
			writer.clear();
		}
		
		if (!currentMessageHasPlurals && level == MESSAGE_LEVEL) {
			keyValues.put(currentKey, Collections.singletonList(writer.toString()));
			writer.clear();
		}
		
		--level;
	}

	@Override
	public Object getResult() throws Exception {
		return null;
	}
	
	public Map<String, List<String>> getKeyValues() {
		return keyValues;
	}

	public int getPluralCount() {
		return nplurals;
	}

	public String getPluralExpression() {
		return plural;
	}
}
