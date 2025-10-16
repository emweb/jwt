package eu.webtoolkit.jwt.examples.planner;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;
import eu.webtoolkit.jwt.Configuration.ErrorReporting;

public class PlannerMain extends WtServlet {
	private static final long serialVersionUID = 1L;

  public PlannerMain() {
    super();

    getConfiguration().setUseScriptNonce(true);
  }

  @Override
	public WApplication createApplication(WEnvironment env) {
		getConfiguration().setErrorReporting(ErrorReporting.NoErrors);
		
		return new PlannerApplication(env);
	}
}
