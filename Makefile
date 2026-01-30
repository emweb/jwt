JWT_VERSION=4.12.2

# Servlet classifier: `jakarta` for Servlet >= 6
ifndef CLASSIFIER
  CLASSIFIER := javax
endif
JWT_JAR := jwt-$(JWT_VERSION)-$(CLASSIFIER).jar
JWT_AUTH_JAR := jwt-auth-$(JWT_VERSION)-$(CLASSIFIER).jar
SERVLET_SRC_PATH := src-$(CLASSIFIER)
CLASSIFIER_M4_ARGS := -DCLASSIFIER=$(CLASSIFIER)

ifeq ($(CLASSIFIER), javax)
  SERVLET_JAR := lib/javax/servlet-api/jakarta.servlet-api-4.0.4.jar
else
  SERVLET_JAR := lib/jakarta/servlet-api/jakarta.servlet-api-6.1.0.jar
endif

.PHONY: all src test examples pom build.xml classpath

pom: jwt-$(JWT_VERSION).pom.in jwt-auth-$(JWT_VERSION).pom.in
	m4 $(CLASSIFIER_M4_ARGS) jwt-$(JWT_VERSION).pom.in > jwt-$(JWT_VERSION).pom ; \
  m4 $(CLASSIFIER_M4_ARGS) jwt-auth-$(JWT_VERSION).pom.in > jwt-auth-$(JWT_VERSION).pom ;

build.xml: build.xml.in
	m4 -DJWT_VERSION=$(JWT_VERSION) $(CLASSIFIER_M4_ARGS) $< > $@

classpath:
	m4 $(CLASSIFIER_M4_ARGS) .classpath.in > .classpath

src:
	for i in src/eu/webtoolkit/jwt/*.java.in \
					 src/eu/webtoolkit/jwt/auth/*.java.in \
					 src/eu/webtoolkit/jwt/auth/mfa/*.java.in \
					 src/eu/webtoolkit/jwt/chart/*.java.in \
					 src/eu/webtoolkit/jwt/render/*.java.in \
					 src/eu/webtoolkit/jwt/servlet/*.java.in \
					 src/eu/webtoolkit/jwt/thirdparty/qrcodegen/*.java.in \
					 src/eu/webtoolkit/jwt/utils/*.java.in; \
		do if test -f $$i ; then \
			javaFile=$$(echo $$i | sed 's/.java.in$$/.java/') ; \
			m4 -P $(CLASSIFIER_M4_ARGS) $$i > $$javaFile; \
		fi; \
	done

test:
	for i in test/eu/webtoolkit/jwt/*.java.in; \
		do if test -f $$i ; then \
			javaFile=$$(echo $$i | sed 's/.java.in$$/.java/') ; \
			m4 $(CLASSIFIER_M4_ARGS) $$i > $$javaFile; \
		fi; \
	done

examples:
	for i in examples/* examples/feature/*; \
		do if test -d $$i -a -e $$i/.classpath.in ; then \
			m4 -DJWT_JAR=$(JWT_JAR) -DJWT_AUTH_JAR=$(JWT_AUTH_JAR) -DSERVLET_JAR=$(SERVLET_JAR) $(CLASSIFIER_M4_ARGS) \
			$$i/.classpath.in > $$i/.classpath; \
		fi; \
		if test -d $$i -a -e $$i/build.xml.in ; then \
			m4 $(CLASSIFIER_M4_ARGS) $$i/build.xml.in > $$i/build.xml; \
		fi; \
	done
	for i in examples/widgetgallery/src/eu/webtoolkit/jwt/examples/widgetgallery/*.java.in \
						examples/filetreetable/src/eu/webtoolkit/jwt/examples/filetreetable/*.java.in; \
		do if test -f $$i ; then \
			javaFile=$$(echo $$i | sed 's/.java.in$$/.java/') ; \
			m4 -P $(CLASSIFIER_M4_ARGS) $$i > $$javaFile; \
		fi; \
	done

all: pom build.xml classpath src test examples

clean-pom:
	if test -f jwt-$(JWT_VERSION).pom -o -f jwt-auth-$(JWT_VERSION).pom ; then \
		ant clean ; \
		rm -f jwt-$(JWT_VERSION).pom jwt-auth-$(JWT_VERSION).pom ; \
	fi

clean-buildxml:
	if test -f build.xml ; then \
		rm -f build.xml ; \
	fi

clean-classpath:
	if test -f .classpath ; then \
		rm -f .classpath ; \
	fi

clean-java:
	for i in src/eu/webtoolkit/jwt/*.java.in \
					 src/eu/webtoolkit/jwt/auth/*.java.in \
					 src/eu/webtoolkit/jwt/auth/mfa/*.java.in \
					 src/eu/webtoolkit/jwt/chart/*.java.in \
					 src/eu/webtoolkit/jwt/render/*.java.in \
					 src/eu/webtoolkit/jwt/servlet/*.java.in \
					 src/eu/webtoolkit/jwt/thirdparty/qrcodegen/*.java.in \
					 src/eu/webtoolkit/jwt/utils/*.java.in \
					 test/eu/webtoolkit/jwt/*.java.in \
					 examples/widgetgallery/src/eu/webtoolkit/jwt/examples/widgetgallery/*.java.in \
					 examples/filetreetable/src/eu/webtoolkit/jwt/examples/filetreetable/*.java.in ; \
		do if test -f $$i ; then \
      javaFile=$$(echo $$i | sed 's/.java.in$$/.java/') ; \
      if test -f $$javaFile ; then \
				rm $$javaFile ; \
			fi ; \
    fi; \
	done ; \

clean-examples:
	for i in examples/* examples/feature/*; \
		do if test -d $$i -a -e $$i/.classpath ; then \
			rm $$i/.classpath ; \
		fi; \
		if test -d $$i -a -e $$i/build.xml ; then \
			rm $$i/build.xml ; \
		fi; \
	done

clean: clean-pom clean-examples clean-java clean-buildxml clean-classpath
