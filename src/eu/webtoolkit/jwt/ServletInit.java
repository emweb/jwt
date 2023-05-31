package eu.webtoolkit.jwt;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletInit implements ServletContextListener {
	private ServletApi servletApi;
	private static Logger logger = LoggerFactory.getLogger(ServletInit.class);

	static ServletInit instance = null;
	
	public ServletInit() {
		instance = this;
	}
	
	static ServletInit getInstance(ServletContext context) {
		if (instance == null) {
			instance = new ServletInit();
			instance.initServletApi(context, false);
		}
		
		return instance;
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext c = event.getServletContext();
		initServletApi(c, true);
	}
	
	private void initServletApi(ServletContext context, boolean contextIsInitializing) {
		if (servletApi == null) {
			try {
				if (context.getMajorVersion() >= 4) {
					logger.info("Using servlet API 4");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi4").getDeclaredConstructor().newInstance();
				} else {
					logger.info("Using servlet API 3");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi3").getDeclaredConstructor().newInstance();
				}
				servletApi.init(context, contextIsInitializing);
			} catch (NoSuchMethodException e) {
				logger.error("", e);
			} catch (InstantiationException e) {
				logger.error("", e);
			} catch (IllegalAccessException e) {
				logger.error("", e);
			} catch (ClassNotFoundException e) {
				logger.error("", e);
			} catch (IllegalArgumentException e) {
				logger.error("", e);
			} catch (InvocationTargetException e) {
				logger.error("", e);
			} catch (SecurityException e) {
				logger.error("", e);
			}
		}
	}

	public ServletApi getServletApi() {
		return servletApi;
	}
}
