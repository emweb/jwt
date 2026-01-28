package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import eu.webtoolkit.jwt.LinkType;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WLink;

public class WApplicationTest {

  static WApplication createApplication() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    return new WApplication(env);
  }

  @Test
  public void testMakeAbsoluteUrlOfAlreadyAbsoluteUrl() {
    WApplication app = createApplication();
    String url = "https://www.emweb.be/app";

    assertEquals(url, app.makeAbsoluteUrl(url));
  }

  @Test
  public void testMakeAbsoluteUrlRemovesDotPrefix() {
    WApplication app = createApplication();

    assertEquals("app", app.makeAbsoluteUrl(".app"));
  }

  @Test
  public void testMakeAbsoluteUrlOnSingleDot() {
    WApplication app = createApplication();

    assertEquals("", app.makeAbsoluteUrl("."));
  }
}
