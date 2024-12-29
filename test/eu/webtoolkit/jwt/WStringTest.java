package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class WStringTest {
	@Test
	public void testTr_XML() {
		Configuration configuration = new Configuration();
		WTestEnvironment env = new WTestEnvironment(configuration);
		WApplication app = new WApplication(env);
		WXmlLocalizedStrings translator = new WXmlLocalizedStrings();
		translator.use("/eu/webtoolkit/jwt/test/plural");
		app.setLocalizedStrings(translator);
		
		assertEquals("Monday", WString.tr("Wt.WDate.Monday").toString());
		assertEquals("The number must be larger than 5", WString.tr("Wt.WDoubleValidator.TooSmall").arg(5).toString());
		assertEquals("0 hours", WString.trn("Wt.WDateTime.hours", 0).arg(0).toString());
		assertEquals("one hour", WString.trn("Wt.WDateTime.hours", 1).arg(1).toString());
		assertEquals("2 hours", WString.trn("Wt.WDateTime.hours", 2).arg(2).toString());
		
		assertEquals("0 files", WString.trn("file", 0).arg(0).toString());
		assertEquals("1 file", WString.trn("file", 1).arg(1).toString());
		assertEquals("2 files", WString.trn("file", 2).arg(2).toString());
		
		assertEquals("??welcome??", WString.tr("welcome").toString());
		
		assertEquals("0 programmers", WString.trn("programmer", 0).arg(0).toString());
		assertEquals("1 programmer", WString.trn("programmer", 1).arg(1).toString());
		assertEquals("6 programmers", WString.trn("programmer", 6).arg(6).toString());
		
		Locale polish = new Locale("pl", "PL");
		app.setLocale(polish);
		
		assertEquals("0 pliko'w", WString.trn("file", 0).arg(0).toString());
		assertEquals("1 plik", WString.trn("file", 1).arg(1).toString());
		assertEquals("2 pliki", WString.trn("file", 2).arg(2).toString());
		assertEquals("6 pliko'w", WString.trn("file", 6).arg(6).toString());
		assertEquals("23 pliki", WString.trn("file", 23).arg(23).toString());
		assertEquals("26 pliko'w", WString.trn("file", 26).arg(26).toString());
		
		assertEquals("??welcome??", WString.tr("welcome").toString());
		
		assertEquals("0 programmers", WString.trn("programmer", 0).arg(0).toString());
		assertEquals("1 programmer", WString.trn("programmer", 1).arg(1).toString());
		assertEquals("6 programmers", WString.trn("programmer", 6).arg(6).toString());
				
		app.remove();
	}

	@Test
	public void testTr_Std() {
		Configuration configuration = new Configuration();
		WTestEnvironment env = new WTestEnvironment(configuration);
		WApplication app = new WApplication(env);
		WStdLocalizedStrings translator = new WStdLocalizedStrings();
		translator.use("eu.webtoolkit.jwt.test.MyResources");
		app.setLocalizedStrings(translator);

		assertEquals("Button", WString.tr("button").toString());
		assertEquals("Shipping & Receiving", WString.tr("shipping_receiving").toString());
		assertEquals("Shipping &amp; Receiving", WString.tr("shipping_receiving").toXhtml());

		assertEquals("??welcome??", WString.tr("welcome").toString());

		Locale dutchLocale = new Locale("nl", "BE");
		app.setLocale(dutchLocale);

		assertEquals("Knop", WString.tr("button").toString());

		assertEquals("??welcome??", WString.tr("welcome").toString());

		app.remove();
	}
	
	@Test
	public void testEscape() {
		Configuration configuration = new Configuration();
		WTestEnvironment env = new WTestEnvironment(configuration);
		WApplication app = new WApplication(env);
		WXmlLocalizedStrings translator = new WXmlLocalizedStrings();
		translator.use("/eu/webtoolkit/jwt/test/escape_test");
		app.setLocalizedStrings(translator);
		
		assertEquals("&amp;", WString.tr("escape_once").toString());
		assertEquals("&amp;amp;", WString.tr("escape_once").toXhtml());
		assertEquals("&#38;", WString.tr("escape_once_numeric").toString());
		assertEquals("&#38;#38;", WString.tr("escape_once_numeric").toXhtml());
		
		app.remove();
	}

	@Test
	public void testDefaultStrings() {
		Configuration configuration = new Configuration();
		WTestEnvironment env = new WTestEnvironment(configuration);
		WApplication app = new WApplication(env);
		WStdLocalizedStrings translator = new WStdLocalizedStrings();
		translator.use("eu.webtoolkit.jwt.test.MyResources");
		app.setLocalizedStrings(translator);

		assertEquals("Monday", WString.tr("Wt.WDate.Monday").toString());
		assertEquals("Button", WString.tr("button").toString());

		app.remove();
	}
}
