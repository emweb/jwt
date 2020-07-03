package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WTreeViewTest {
	@Test
	public void test_TreeView1() {
		Configuration configuration = new Configuration();
		WTestEnvironment env = new WTestEnvironment(configuration);
		WApplication app = new WApplication(env);

		WStandardItemModel model = new WStandardItemModel();

		WStandardItem root = model.getInvisibleRootItem();
		for (int i = 0; i < 6; ++i) {
			WStandardItem item = new WStandardItem("level 1, row " + i);
			for (int j = 0; j < 4; ++j) {
				WStandardItem subItem = new WStandardItem("level 2, row " + j);
				for (int k = 0; k < 3; ++k) {
					WStandardItem subsubItem = new WStandardItem("level 3, row " + k);
					subsubItem.appendRow(new WStandardItem("level 4"));
					subItem.appendRow(subsubItem);
				}
				item.appendRow(subItem);
			}
			root.appendRow(item);
		}

		app.remove();

		WTreeView tree = new WTreeView(app.getRoot());
        tree.setModel(model);

		tree.expand(model.getIndex(2, 0));
		tree.expand(model.getIndex(1, 0, model.getIndex(2, 0)));
		tree.expand(model.getIndex(0, 0, model.getIndex(1, 0, model.getIndex(2, 0))));
		tree.expand(model.getIndex(3, 0));

		assertFalse(tree.isExpanded(model.getIndex(0, 0)));
		assertFalse(tree.isExpanded(model.getIndex(1, 0)));
		assertTrue(tree.isExpanded(model.getIndex(2, 0)));
		assertTrue(tree.isExpanded(model.getIndex(1, 0, model.getIndex(2, 0))));
		assertTrue(tree.isExpanded(model.getIndex(0, 0, model.getIndex(1, 0, model.getIndex(2, 0)))));
		assertTrue(tree.isExpanded(model.getIndex(3, 0)));
		assertTrue(!tree.isExpanded(model.getIndex(4, 0)));
		assertTrue(!tree.isExpanded(model.getIndex(5, 0)));

		model.removeRows(2, 2);

		assertEquals(4, model.getRowCount());
		assertFalse(tree.isExpanded(model.getIndex(0, 0)));
		assertFalse(tree.isExpanded(model.getIndex(1, 0)));
		assertFalse(tree.isExpanded(model.getIndex(2, 0)));
		assertFalse(tree.isExpanded(model.getIndex(3, 0)));
		assertFalse(tree.isExpanded(model.getIndex(1, 0, model.getIndex(2, 0))));
		assertFalse(tree.isExpanded(model.getIndex(0, 0, model.getIndex(1, 0, model.getIndex(2, 0)))));
	}
}
