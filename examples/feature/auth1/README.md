Auth1 example
-------------------

This example demonstrates how to use the JWt authentication library.

The example uses the lib/jwt-lib/javamail/geronimo-javamail_1.4_*.jar jar 
files only for compiling the project. The jars are not included in the 
war file, since some servlet containers come with the javamail API included 
(e.g. Jetty) and all J2EE servers are supposed to include the javamail API. 
However, some servlet containers, Apache Tomcat for example,
do not include the javamail API, and for these containers, you will need
to implicitely include the geronimo-javamail_1.4_*.jar jar files. This can
be done by adding the following line of XML to the build.xml to the war 
element in the dist target:
<lib dir="${jwt-lib.dir}/javamail/" includes="*.jar" />

The pom.xml demonstrates how JWt Auth can be used from within a maven 
project, the geronimo-javamail_1.4_mail dependency is scoped as "provided".
This scope will only use the dependency for compiling the project.
When using targeting servlet containers that have no support
for the javamail API, you will need to remove this scope declaration,
to allow the dependency to be included in your target war.
