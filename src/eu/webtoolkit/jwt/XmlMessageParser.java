package eu.webtoolkit.jwt;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.n3.nanoxml.IXMLBuilder;

public class XmlMessageParser implements IXMLBuilder {
	private Map<String, String> keyValues = new HashMap<String, String>(); 
	private EscapeOStream writer = new EscapeOStream();
	
	private boolean tagOpen = false;
	private List<String> level = new ArrayList<String>();
	private String currentKey = null;
	
	private final static int MESSAGE_LEVEL = 2;
	private final static String KEY_STRING = "id";

	public XmlMessageParser() {
		super();
	}

	public void addExternalEntity(String name, String publicID, String systemID) {
	}

	public void addInternalEntity(String name, String value) {
	}

	public void newProcessingInstruction(String target, Reader reader) throws Exception {
	}

	public void startBuilding(String systemID, int lineNr) throws Exception {
	}

	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (level.size() == MESSAGE_LEVEL && KEY_STRING.equals(key)) 
			currentKey = value;
		
		if (level.size() > MESSAGE_LEVEL) {
			writer.append(' ' + key + "=\"");
			writer.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
			writer.append(value);
			writer.popEscape();
			writer.append('"');
		}
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (level.size() >= MESSAGE_LEVEL) {
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
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (level.size() > MESSAGE_LEVEL) 
			tagOpen = true;
	}

	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		level.add(name);
		
		if (tagOpen) {
			writer.append('>');
			tagOpen = false;
		}

		if (level.size() > MESSAGE_LEVEL) { 
			writer.append('<');
			writer.append(name);
		}
	}

	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (level.size() > MESSAGE_LEVEL) {
			if (tagOpen && DomElement.isSelfClosingTag(name)) {
				writer.append("/>");
			} else {
				if (tagOpen)
					writer.append('>');
				writer.append("</");
				writer.append(name);
				writer.append('>');
			}
	
			tagOpen = false;
		}
		
		if (level.size() == MESSAGE_LEVEL) {
			keyValues.put(currentKey, writer.toString());
			writer.clear();
		}
		
		level.remove(level.size() - 1);
	}

	@Override
	public Object getResult() throws Exception {
		return null;
	}
	
	public Map<String, String> getKeyValues() {
		return keyValues;
	}
}
