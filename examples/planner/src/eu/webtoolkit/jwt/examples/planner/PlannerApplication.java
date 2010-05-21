package eu.webtoolkit.jwt.examples.planner;

import java.io.IOException;
import java.util.EnumSet;

import javax.persistence.EntityManager;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WCombinedLocalizedStrings;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WEvent;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WStdLocalizedStrings;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.examples.planner.calendar.PlannerCalendar;
import eu.webtoolkit.jwt.examples.planner.data.UserAccount;
import eu.webtoolkit.jwt.examples.planner.jpa.JpaUtil;

public class PlannerApplication extends WApplication {
	private EntityManager entityManager;
	
	public PlannerApplication(WEnvironment env) {
		super(env);
		
		//set the application title
		setTitle("JWt Planner");
		
		//load the application resource bundles
		//this application uses both xml and java resourcebundles,
		//therefore we need a WCombinedLocalizedStrings
		WCombinedLocalizedStrings cls = new WCombinedLocalizedStrings();
		
		//load the java resourcebundles
		WStdLocalizedStrings wsls = new WStdLocalizedStrings();
	    wsls.use("eu.webtoolkit.jwt.examples.planner.planner");
	    wsls.use("eu.webtoolkit.jwt.examples.planner.captcha.captcha");
	    cls.add(wsls);
	    
	    //load the xml resourcebundle 
	    WXmlLocalizedStrings wxls = new WXmlLocalizedStrings();
	    wxls.use("/eu/webtoolkit/jwt/examples/planner/calendar/calendar");
	    cls.add(wxls);
	    
	    //connect the resourcebundle to the application object
	    setLocalizedStrings(cls);
	    
	    //set the css stylesheet
	    useStyleSheet("style/planner.css");

	    //construct a login widget
	    LoginWidget login = new LoginWidget(getRoot());

	    //add a listener to the logged in signal,
	    //find the user in the database (or create a new one)
	    //and show the calendar widget
	    login.loggedIn().addListener(this, new Signal1.Listener<String>(){
			public void trigger(String user) {
				getRoot().clear();

				entityManager = JpaUtil.getEntityManager();
				UserAccount ua = UserAccount.login(entityManager, user);
				new PlannerCalendar(getRoot(), ua);
			}
	    });
	}
	
	@Override
	/*
	 * Show a fatal error when an unexpected database error occurs, 
	 * and in order to free the session, rollback the transaction when it is active. 
	 */
	protected void notify(WEvent e) throws IOException {
		try {
			super.notify(e);
		} catch (Exception e1) {
			fatalError(e1);
		} finally {
			if (entityManager != null && entityManager.getTransaction().isActive()) {
				try {
					entityManager.getTransaction().rollback();
				} catch (Exception e2) {
					fatalError(e2);
				}
			}
		}
	}

	private void fatalError(Exception err) {
		WMessageBox box = new WMessageBox(tr("fatalerror.title"), tr("fatalerror.body").arg(err.getMessage()),
				null, EnumSet.of(StandardButton.Ok));
		box.show();
		
		err.printStackTrace();
		
		quit();
	}
}
