package eu.webtoolkit.jwt;

import org.junit.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class WStdLocalizedStringsTest {
    @Test
    public void bundleTest() {
        ResourceBundle bundle = ResourceBundle.getBundle("eu.webtoolkit.jwt.test.MyResources", new Locale(""));
        assertNotNull(bundle);

        assertEquals("Button", bundle.getString("button"));

        ResourceBundle bundle_nl = ResourceBundle.getBundle("eu.webtoolkit.jwt.test.MyResources", new Locale("nl"));
        assertNotNull(bundle_nl);

        assertEquals("Knop", bundle_nl.getString("button"));
    }

    @Test
    public void stdLocalizedStringsTest() {
        WStdLocalizedStrings localizedStrings = new WStdLocalizedStrings();
        localizedStrings.use("eu.webtoolkit.jwt.test.MyResources");

        LocalizedString result = localizedStrings.resolveKey(new Locale(""), "button");
        assertTrue(result.success);
        assertEquals("Button", result.value);
        assertEquals(TextFormat.Plain, result.format);

        result = localizedStrings.resolveKey(new Locale("nl"), "button");
        assertTrue(result.success);
        assertEquals("Knop", result.value);
        assertEquals(TextFormat.Plain, result.format);
    }
}
