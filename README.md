What is JWt ?
------------

JWt is a Java library for developing web applications. It provides a pure
Java component-driven approach to building web applications,
and renders either using Ajax or plain HTML.

Unlike JSF, there is no concept of a page and no split between page
"views" and reusable "components", making reuse unpractical. Instead,
everything is a widget that can be reused in other widgets.

For more information, see [the homepage](http://www.webtoolkit.eu/jwt
"JWt homepage").

Dependencies
------------

The library requires a Servlet 6.1 (`jakarta.servlet` namespace) container.
It is able to use asynchronous I/O functionality
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

    mvn install:install-file -Dfile=dist/jwt-4.12.2.jar -DpomFile=jwt-4.12.2.pom 
    mvn install:install-file -Dfile=dist/jwt-auth-4.12.2.jar -DpomFile=jwt-auth-4.12.2.pom 

The corresponding dependency blocks are:

    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt</artifactId>
      <version>4.12.2</version>
    </dependency>

    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.1.0</version>
      <scope>provided</scope>
    </dependency>

There are a number of optional dependencies for JWt, needed only depending on what
features you use

    <!-- optional, for JWT Auth -->
    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt-auth</artifactId>
      <version>4.12.2</version>
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
      <artifactId>antlr4-runtime</artifactId>
      <version>4.7.2</version>
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
      <groupId>org.eclipse.angus</groupId>
      <artifactId>angus-mail</artifactId>
      <version>2.0.3</version>
      <scope>provided</scope>
    </dependency>
