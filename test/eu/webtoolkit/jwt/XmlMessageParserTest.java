package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class XmlMessageParserTest {
	private static class Result {
		Map<String, List<String>> map = new HashMap<>();
		int pluralCount = 0;
		String pluralExpression = "";
	}
	
	public Result readXml(InputStream stream) throws Exception {
		XmlMessageParser xmlParser = new XmlMessageParser();
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setBuilder(xmlParser);
		parser.setResolver(xmlParser);
		IXMLReader reader = new StdXMLReader(stream);
		parser.setReader(reader);
		parser.parse();
		
		Result result = new Result();
		
		result.map.putAll(xmlParser.getKeyValues());
		result.pluralCount = xmlParser.getPluralCount();
		result.pluralExpression = xmlParser.getPluralExpression();
		
		return result;
	}
	
	@Test
	public void wtXmlTest() throws Exception {
		Result result = readXml(FileUtils.getResourceAsStream("/eu/webtoolkit/jwt/wt.xml"));
		
		assertEquals(2, result.pluralCount);
		assertEquals("n == 1 ? 0 : 1", result.pluralExpression);
		
		assertEquals("Monday", result.map.get("Wt.WDate.Monday").get(0));
		assertEquals(2, result.map.get("Wt.WDateTime.seconds").size());
		assertEquals("one second", result.map.get("Wt.WDateTime.seconds").get(0));
		assertEquals("{1} seconds", result.map.get("Wt.WDateTime.seconds").get(1));
		assertEquals("one week", result.map.get("Wt.WDateTime.weeks").get(0));
		assertEquals("{1} weeks", result.map.get("Wt.WDateTime.weeks").get(1));
		assertEquals("one year", result.map.get("Wt.WDateTime.years").get(0));
		assertEquals("{1} years", result.map.get("Wt.WDateTime.years").get(1));
		assertEquals("", result.map.get("Wt.WDateTime.null").get(0));
	}
	
	@Test
	public void polishTest() throws Exception {
		Result result = readXml(FileUtils.getResourceAsStream("/eu/webtoolkit/jwt/test/plural_pl.xml"));
		
		assertEquals(3, result.pluralCount);
		assertEquals("n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2", result.pluralExpression);
		
		assertEquals(3, result.map.get("file").size());
		assertEquals("{1} plik", result.map.get("file").get(0));
		assertEquals("{1} pliki", result.map.get("file").get(1));
		assertEquals("{1} pliko'w", result.map.get("file").get(2));
	}
}
