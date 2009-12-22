/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

class ToggleButton extends WText {
	public ToggleButton(ToggleButtonConfig config, WContainerWidget parent) {
		super(parent);
		this.signals_ = new ArrayList<JSignal>();
		this.config_ = config;
		this.setInline(false);
		this.clicked().addListener(this.config_.toggleJS_);
		for (int i = 0; i < this.config_.getStates().size(); ++i) {
			this.signals_.add(new JSignal(this, "t-"
					+ this.config_.getStates().get(i)));
		}
	}

	public ToggleButton(ToggleButtonConfig config) {
		this(config, (WContainerWidget) null);
	}

	public void remove() {
		for (int i = 0; i < this.signals_.size(); ++i) {
			;
		}
		super.remove();
	}

	public JSignal signal(int i) {
		return this.signals_.get(i);
	}

	public void setState(int i) {
		this.setStyleClass(this.config_.getStates().get(i));
	}

	private List<JSignal> signals_;
	private ToggleButtonConfig config_;
}
