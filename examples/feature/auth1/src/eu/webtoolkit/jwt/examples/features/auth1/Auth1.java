package eu.webtoolkit.jwt.examples.features.auth1;

import javax.servlet.ServletException;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class Auth1 extends WtServlet {
	private static final long serialVersionUID = 1L;

  public Auth1() {
    super();

    getConfiguration().setUseScriptNonce(true);
  }

	@Override
	public WApplication createApplication(WEnvironment env) {
		return new AuthApplication(env);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
	}
}
