package eu.webtoolkit.jwt;

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
				if (context.getMajorVersion() == 3) {
					logger.info("Using servlet API 3");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi3").newInstance();
				} else {
					logger.info("Using servlet API 2.5");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi25").newInstance();
				}
				
				servletApi.init(context, contextIsInitializing);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public ServletApi getServletApi() {
		return servletApi;
	}
}
