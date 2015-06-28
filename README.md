What is JWt ?
------------

JWt is a Java library for developing web applications. It provides a pure
Java component-driven approach to building web applications,
and renders either using Ajax or plain HTML.

Unlike JSF, there is no concept of a page and no split between page
"views" and reusable "components", making reuse unpractical. Instead,
everything is a widget that can be resued in other widgets.

For more information, see [the homepage](http://www.webtoolkit.eu/jwt
"JWt homepage").

Dependencies
------------

The library requires a Servlet 2.5 or 3.0 container. When deployed in a
servlet 3.0 container, it is able to use asynchronous I/O functionality
to improve scalability when using server push features.

If you want to use the PDF rendering support (the WPdfImage and
WPdfRenderer classes), then you also need to add
PdfJet[http://pdfjet.com/] to your project.

Building
--------

It can be as simple as:

    ant

Demos, examples
---------------

The homepage contains [various examples](http://www.webtoolkit.eu/jwt/examples).

Maven
-----

The ant build file has a separate target to generate maven pom files:

    ant mvn

To install the two artifacts in your local repository, do:

    mvn install:install-file -Dfile=dist/jwt-3.3.2.jar -DpomFile=jwt-3.3.2.pom
    mvn install:install-file -Dfile=dist/jwt-auth-3.3.2.jar -DpomFile=jwt-auth-3.3.2.pom

The corresponding dependency blocks are:

    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt</artifactId>
      <version>3.3.2</version>
    </dependency>

    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>servlet-api</artifactId>
      <version>2.5</version>
    </dependency>

There are a number of optional dependencies for JWt, needed only depending on what
features you use

    <!-- optional, for JWT Auth -->
    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt-auth</artifactId>
      <version>3.3.2</version>
    </dependency>

    <!-- optional, for PDF Rendering -->
    <dependency>
      <groupId>com.pdfjet</groupId>
      <artifactId>pdfjet</artifactId>
      <version>4.75</version>
    </dependency>

    <!-- optional, for CSS stylesheet support in XHTML renderer -->
    <dependency>
      <groupId>org.antlr</groupId>
      <artifactId>antlr-runtime</artifactId>
      <version>3.2</version>
    </dependency>

    <!-- optional, for server-side WebGL fallback -->
    <dependency>
      <groupId>org.jogamp.jogl</groupId>
      <artifactId>jogl-all</artifactId>
      <version>2.0-rc11</version>
    </dependency>

    <!-- optional, for server-side WebGL fallback -->
    <dependency>
      <groupId>org.jogamp.gluegen</groupId>
      <artifactId>gluegen-rt-main</artifactId>
      <version>2.0-rc11</version>
    </dependency>

    <!-- may be needed if your J2EE container doesn't provide this -->
    <dependency>
      <groupId>org.apache.geronimo.javamail</groupId>
      <artifactId>geronimo-javamail_1.4_mail</artifactId>
      <version>1.8.1</version>
      <scope>provided</scope>
    </dependency>
