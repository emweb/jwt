package eu.webtoolkit.jwt.examples.features.auth1;

import javax.servlet.ServletException;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.examples.features.auth1.model.Session;

public class Auth1 extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env) {
		return new AuthApplication(env);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
	}
}
