package eu.webtoolkit.jwt.examples.treeview;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class TreeViewMain extends WtServlet {
	public TreeViewMain() {
		super();
		getConfiguration().setSendXHTMLMimeType(true);
	}

	public WApplication createApplication(WEnvironment env) {
		  WApplication app = new TreeViewApplication(env);
		  app.setTitle("WTreeView example");
		  
		  return app;
	}
}
