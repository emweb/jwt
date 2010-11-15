package eu.webtoolkit.jwt;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

@WebListener
/**
 * This implementation recieves notifications about changes to the servlet 
 * context.
 */
public class WtServletContextListener implements ServletContextListener {
  /**
   * Apply the url session tracking mode.
   */
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		Set<SessionTrackingMode> trackingMode = new HashSet<SessionTrackingMode>();
		trackingMode.add(SessionTrackingMode.URL);
		sc.setSessionTrackingModes(trackingMode);
	}

  /**
   * Do nothing.
   */
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
