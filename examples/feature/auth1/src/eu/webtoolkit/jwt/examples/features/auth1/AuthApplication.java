package eu.webtoolkit.jwt.examples.features.auth1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.auth.AuthWidget;
import eu.webtoolkit.jwt.examples.features.auth1.jpa.JpaUtil;
import eu.webtoolkit.jwt.examples.features.auth1.model.Session;

public class AuthApplication extends WApplication {
	private static Logger logger = LoggerFactory
			.getLogger(AuthApplication.class);

	public AuthApplication(WEnvironment env) {
		super(env);
		
		Session.configureAuth();
		
		session_ = new Session(JpaUtil.getEntityManager());

		session_.getLogin().changed().addListener(this, new Signal.Listener() {

			@Override
			public void trigger() {
				authEvent();

			}
		});

		useStyleSheet("css/style.css");

		AuthWidget authWidget = new AuthWidget(Session.getAuth(),
				session_.getUserDatabase(), session_.getLogin());

		authWidget.getModel().addPasswordAuth(Session.getPasswordAuth());
		authWidget.getModel().addOAuth(Session.getOAuth());
		authWidget.setRegistrationEnabled(true);

		authWidget.processEnvironment();

		getRoot().addWidget(authWidget);
	}

	public void authEvent() {
		if (session_.getLogin().isLoggedIn())
			logger.info("User " + session_.getLogin().getUser().getId()
					+ " logged in.");
		else
			logger.info("User logged out.");
	}

	private Session session_;
}
