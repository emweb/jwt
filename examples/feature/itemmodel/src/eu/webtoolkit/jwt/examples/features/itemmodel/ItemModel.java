package eu.webtoolkit.jwt.examples.features.itemmodel;

import java.io.File;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WtServlet;


public class ItemModel extends WtServlet implements Signal.Listener {
	private static final long serialVersionUID = 1L;

  public ItemModel() {
    super();

    getConfiguration().setUseScriptNonce(true);
  }

	@Override
	public WApplication createApplication(WEnvironment env) {
		WApplication app = new WApplication(env);

		new WLabel("Path:", app.getRoot());
		pathLE = new WLineEdit(app.getRoot());
		pathLE.enterPressed().addListener(app.getRoot(), this);
		WPushButton refreshB = new WPushButton("Refresh", app.getRoot()); 
		refreshB.clicked().addListener(app.getRoot(), this);
		new WBreak(app.getRoot());
		
		imv = new ItemModelView(app.getRoot());
		
		return app;
	}
	
	public void trigger() {
		imv.setRoot(new File(pathLE.getText()));
	}
	
	private WLineEdit pathLE;
	private ItemModelView imv;
}
