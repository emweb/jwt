package eu.webtoolkit.jwt;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

@WebListener
public class WtServletContextListener implements ServletContextListener {
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		Set<SessionTrackingMode> trackingMode = new HashSet<SessionTrackingMode>();
		trackingMode.add(SessionTrackingMode.URL);
		sc.setSessionTrackingModes(trackingMode);
	}

	public void contextDestroyed(ServletContextEvent sce) {
	}
}
