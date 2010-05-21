package eu.webtoolkit.jwt.examples.planner;

import javax.persistence.EntityManager;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WCombinedLocalizedStrings;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WStdLocalizedStrings;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.examples.planner.calendar.PlannerCalendar;
import eu.webtoolkit.jwt.examples.planner.data.UserAccount;
import eu.webtoolkit.jwt.examples.planner.jpa.JpaUtil;

public class PlannerMain extends WtServlet {
	public WApplication createApplication(WEnvironment env) {
		getConfiguration().setDebug(true);
		
		return new PlannerApplication(env);
	}
}
