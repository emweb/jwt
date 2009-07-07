/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.hellowidgetset;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class HelloWidgetSetMain extends WtServlet {
	@Override
	public WApplication createApplication(WEnvironment env)
	{
	  return new HelloWidgetSetApplication(env, true);
	}
}
