/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeviewdragdrop;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.MatchOptions;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WAbstractProxyModel;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WDatePicker;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WIntValidator;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A dialog for editing a 'file'.
 */
public class FileEditDialog extends WDialog {
	public FileEditDialog(WAbstractItemModel model, WModelIndex item) {
		super("Edit...");
		model_ = model;
		item_ = item;
		int modelRow = item_.getRow();

		resize(new WLength(300), WLength.Auto);

		/*
		 * Create the form widgets, and load them with data from the model.
		 */

		// name
		nameEdit_ = new WLineEdit(StringUtils
				.asString(model_.getData(modelRow, 1)).getValue());

		// type
		typeEdit_ = new WComboBox();
		typeEdit_.addItem("Document");
		typeEdit_.addItem("Spreadsheet");
		typeEdit_.addItem("Presentation");
		typeEdit_.setCurrentIndex(typeEdit_.findText(StringUtils
				.asString(model_.getData(modelRow, 2)), MatchOptions.defaultMatchOptions));

		// size
		sizeEdit_ = new WLineEdit(StringUtils
				.asString(model_.getData(modelRow, 3)).getValue());
		sizeEdit_.setValidator(new WIntValidator(0, Integer.MAX_VALUE, this));

		// created
		createdPicker_ = new WDatePicker();
		createdPicker_.getLineEdit().getValidator().setMandatory(true);
		createdPicker_.setFormat(FileModel.dateEditFormat);
		createdPicker_.setDate((WDate) (model_.getData(modelRow, 4)));

		// modified
		modifiedPicker_ = new WDatePicker();
		modifiedPicker_.getLineEdit().getValidator().setMandatory(true);
		modifiedPicker_.setFormat(FileModel.dateEditFormat);
		modifiedPicker_.setDate((WDate) (model_.getData(modelRow, 5)));

		/*
		 * Use a grid layout for the labels and fields
		 */
		WGridLayout layout = new WGridLayout();

		WLabel l;
		int row = 0;

		layout.addWidget(l = new WLabel("Name:"), row, 0);
		layout.addWidget(nameEdit_, row, 1);
		l.setBuddy(nameEdit_);
		++row;

		layout.addWidget(l = new WLabel("Type:"), row, 0);
		layout.addWidget(typeEdit_, row, 1, AlignmentFlag.AlignTop);
		l.setBuddy(typeEdit_);
		++row;

		layout.addWidget(l = new WLabel("Size:"), row, 0);
		layout.addWidget(sizeEdit_, row, 1);
		l.setBuddy(sizeEdit_);
		++row;

		layout.addWidget(l = new WLabel("Created:"), row, 0);
		layout.addWidget(createdPicker_.getLineEdit(), row, 1);
		layout.addWidget(createdPicker_, row, 2);
		l.setBuddy(createdPicker_.getLineEdit());
		++row;

		layout.addWidget(l = new WLabel("Modified:"), row, 0);
		layout.addWidget(modifiedPicker_.getLineEdit(), row, 1);
		layout.addWidget(modifiedPicker_, row, 2);
		l.setBuddy(modifiedPicker_.getLineEdit());
		++row;

		WPushButton b;
		WContainerWidget buttons = new WContainerWidget();
		buttons.addWidget(b = new WPushButton("Save"));
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				accept();
			}
		});
		getContents().enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				accept();
			}
		});

		buttons.addWidget(b = new WPushButton("Cancel"));
		b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent a1) {
				reject();
			}
		});

		/*
		 * Focus the form widget that corresponds to the selected item.
		 */
		switch (item.getColumn()) {
		case 2:
			typeEdit_.setFocus();
			break;
		case 3:
			sizeEdit_.setFocus();
			break;
		case 4:
			createdPicker_.getLineEdit().setFocus();
			break;
		case 5:
			modifiedPicker_.getLineEdit().setFocus();
			break;
		default:
			nameEdit_.setFocus();
			break;
		}

		layout.addWidget(buttons, row, 0, 0, 3, AlignmentFlag.AlignCenter);
		layout.setColumnStretch(1, 1);

		getContents().setLayout(layout, AlignmentFlag.AlignTop,
				AlignmentFlag.AlignJustify);

		finished().addListener(this, new Signal1.Listener<DialogCode>() {
			public void trigger(DialogCode dc) {
				handleFinish(dc);
			}

		});

		show();
	}

	private WAbstractItemModel model_;
	private WModelIndex item_;

	private WLineEdit nameEdit_, sizeEdit_;
	private WComboBox typeEdit_;
	private WDatePicker createdPicker_, modifiedPicker_;

	private void handleFinish(DialogCode result) {
		if (result == DialogCode.Accepted) {
			/*
			 * Update the model with data from the edit widgets.
			 * 
			 * You will want to do some validation here...
			 * 
			 * Note that we directly update the source model to avoid problems
			 * caused by the dynamic sorting of the proxy model, which reorders
			 * row numbers, and would cause us to switch to editing the wrong
			 * data.
			 */
			WAbstractItemModel m = model_;
			int modelRow = item_.getRow();

			WAbstractProxyModel proxyModel = (WAbstractProxyModel) m;
			if (proxyModel != null) {
				m = proxyModel.getSourceModel();
				modelRow = proxyModel.mapToSource(item_).getRow();
			}

			m.setData(modelRow, 1, nameEdit_.getText());
			m.setData(modelRow, 2, typeEdit_.getCurrentText());
			m.setData(modelRow, 3, Integer.parseInt(sizeEdit_.getText()));
			m.setData(modelRow, 4, createdPicker_.getDate());
			m.setData(modelRow, 5, modifiedPicker_.getDate());
		}
	}
}