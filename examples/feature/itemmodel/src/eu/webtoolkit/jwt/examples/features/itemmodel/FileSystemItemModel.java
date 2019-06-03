package eu.webtoolkit.jwt.examples.features.itemmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.WItemModel;
import eu.webtoolkit.jwt.WModelIndex;

public class FileSystemItemModel extends WItemModel<File> {
	private File root;
	
	public FileSystemItemModel(File root) {
		super();
		this.root = root;
	}

	@Override
	public List<File> getChildItems(File item, int from, int count) {
		if (item == null)
			item = root;
		
		String [] children = item.list();
		if (children == null)
			return new ArrayList<File>();
		
		Arrays.sort(children);
		
		List<File> files = new ArrayList<File>(children.length);
		for (String child : children) {
			files.add(new File(item.getAbsolutePath() + File.separatorChar + child));
		}
		
		return files;
	}
	
	@Override
	public int getChildCount(File file) {
		if (file == null)
			file = root;

		if (file.list() == null)
			return 0;
		else 
			return file.list().length;
	}

	@Override
	public int getColumnCount(WModelIndex parent) {
		return 2;
	}

	@Override
	public Object getData(WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole)
			return null;
		
		File f = getItem(index);
		if (index.getColumn() == 0)
			return f.getName();
		else if (!f.isDirectory())
			return f.length();
		else
			return "";
	}
}
