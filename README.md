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

The library requires a Servlet 3.0 or 4.0 (`javax.servlet` namespace),
or 6.1 (`jakarta.servlet` namespace) container.
It is able to use asynchronous I/O functionality
to improve scalability when using server push features.

If you want to use the PDF rendering support (the WPdfImage and
WPdfRenderer classes), then you also need to add
PdfJet[http://pdfjet.com/] to your project.

Building
--------

If you are using a Servlet with `javax.servlet` namespace, it can be as
simple as:

    make all
    ant

If you are using a Servlet with `jakarta.servlet` namespace, you will
need to specify it as a parameter for make. Your make command will
then look like this:

    make javax=jakarta all

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

The corresponding dependency blocks will depends on the Servlet
namespace. Here is the what it will look like when using the
`javax.servlet` namespace:

    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt</artifactId>
      <version>4.12.2</version>
      <classifier>javax</classifier>
    </dependency>

    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.4</version>
      <scope>provided</scope>
    </dependency>

There are a number of optional dependencies for JWt, needed only depending on what
features you use

    <!-- optional, for JWT Auth -->
    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt-auth</artifactId>
      <version>4.12.2</version>
      <classifier>javax</classifier>
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
      <!-- if using javax.servlet -->
      <groupId>org.apache.geronimo.javamail</groupId>
      <artifactId>geronimo-javamail_1.4_mail</artifactId>
      <version>1.8.1</version>

      <!-- if using jakarta.servlet -->
      <groupId>org.eclipse.angus</groupId>
      <artifactId>angus-mail</artifactId>
      <version>2.0.3</version>

      <scope>provided</scope>
    </dependency>
