package eu.webtoolkit.jwt;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.servlet.UTF8Filter;

class ServletApi3 extends ServletApi {
	private static Logger logger = LoggerFactory.getLogger(ServletApi3.class);

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void configureRequestEncoding(ServletContext context, boolean contextIsInitializing) {
		if (contextIsInitializing) {
			final var dynamicMapping = context.addFilter("UTF8Filter", UTF8Filter.class);
			dynamicMapping.setAsyncSupported(true);
			dynamicMapping.addMappingForUrlPatterns(null, true, "/*");
			logger.info("Registered request encoding filter: set default character encoding to UTF-8");
		}
	}
}
