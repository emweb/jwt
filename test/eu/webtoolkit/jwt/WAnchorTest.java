package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import eu.webtoolkit.jwt.LinkType;
import eu.webtoolkit.jwt.WAnchor;
import eu.webtoolkit.jwt.WLink;

public class WAnchorTest {
  @Test
  public void testExternalLinkRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink("https://www.emweb.be"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "https://www.emweb.be");
  }

  @Test
  public void testInternalPathLinkRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink(LinkType.InternalPath, "/app"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "app");
  }

  @Test
  public void testInternalPathSamePageLinkRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink(LinkType.InternalPath, "#app"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "app");
  }

  @Test
  public void testInternalPathSamePageSlashLinkRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink(LinkType.InternalPath, "/#app"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "#app");
  }

  @Test
  public void testExternalLinkWithParametersRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink("https://www.emweb.be?param1=1&param2=2"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "https://www.emweb.be?param1=1&param2=2");
  }

  @Test
  public void testInternalPathLinkWithParametersRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink(LinkType.InternalPath, "/app?param1=1&param2=2"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "app%3fparam1%3d1%26param2%3d2");
  }

  @Test
  public void testExternalLinkWithParametersAndSessionIdRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    env.setSessionIdInUrl(true);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink("https://www.emweb.be?param1=1&param2=2"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    // This "&" will be handled by the RefEncoder
    assertEquals(domElement.getAttribute("href"), "?request=redirect&url=https%3a%2f%2fwww.emweb.be%3fparam1%3d1%26param2%3d2&hash=bVKA5XzzgVcoduCJyhjk6w%3d%3d");
  }

  @Test
  public void testInternalPathLinkWithParametersAndSessionIdRender() {
    Configuration configuration = new Configuration();
    WTestEnvironment env = new WTestEnvironment(configuration);
    env.setSessionIdInUrl(true);
    WApplication app = new WApplication(env);

    WAnchor anchor = new WAnchor(new WLink(LinkType.InternalPath, "/app?param1=1&param2=2"));
    app.getRoot().addWidget(anchor);

    // Simulate UI update (and calls updateDom)
    DomElement domElement = anchor.createSDomElement(app);

    assertEquals(domElement.getAttribute("href"), "app%3fparam1%3d1%26param2%3d2");
  }
}
