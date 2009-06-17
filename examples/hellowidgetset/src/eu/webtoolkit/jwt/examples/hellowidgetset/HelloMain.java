package eu.webtoolkit.jwt.examples.hellowidgetset;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class HelloMain extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env)
	{
	  return new HelloWidgetSetApplication(env, false);
	}
}
