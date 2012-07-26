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

    mvn install:install-file -Dfile=dist/jwt-3.2.2.jar -DpomFile=jwt-3.2.2.pom    
    mvn install:install-file -Dfile=dist/jwt-auth-3.2.2.jar -DpomFile=jwt-auth-3.2.2.pom

The corresponding dependency blocks are:

    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt</artifactId>
      <version>3.2.2</version>
    </dependency>

    <dependency>
      <groupId>eu.webtoolkit</groupId>
      <artifactId>jwt-auth</artifactId>
      <version>3.2.2</version>
    </dependency>
    
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>servlet-api</artifactId>
      <version>2.5</version>
    </dependency>

    <dependency>
      <groupId>org.apache.geronimo.javamail</groupId>
      <artifactId>geronimo-javamail_1.4_mail</artifactId>
      <version>1.8.1</version>
      <scope>provided</scope>
    </dependency>

(Depending on the J2EE container, javax.mail may be included and provided by the container,
 and then the last dependency for org.apache.geronimo.javamail should be dropped)
