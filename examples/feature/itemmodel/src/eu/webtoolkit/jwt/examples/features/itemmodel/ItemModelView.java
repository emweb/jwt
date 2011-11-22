package eu.webtoolkit.jwt.examples.features.itemmodel;

import java.io.File;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WTreeView;

public class ItemModelView extends WContainerWidget {
	private WTreeView tv;
	
	public ItemModelView(WContainerWidget parent) {
		super(parent);
	}
	
	public void setRoot(File root) {
		if (tv == null) {
			tv = new WTreeView(this);
			tv.resize(600, 600);
		}
		
		FileSystemItemModel model = new FileSystemItemModel(root);
		tv.setModel(model);
	}
}
