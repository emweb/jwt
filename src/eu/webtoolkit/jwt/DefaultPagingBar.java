/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


class DefaultPagingBar extends WContainerWidget {
	public DefaultPagingBar(WAbstractItemView view) {
		super();
		this.view_ = view;
		this.setStyleClass("Wt-pagingbar");
		this.firstButton_ = new WPushButton(new WString(pbFirst), this);
		this.firstButton_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						DefaultPagingBar.this.showFirstPage();
					}
				});
		this.prevButton_ = new WPushButton(new WString(pbPrevious), this);
		this.prevButton_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						DefaultPagingBar.this.showPreviousPage();
					}
				});
		this.current_ = new WText(this);
		this.nextButton_ = new WPushButton(new WString(pbNext), this);
		this.nextButton_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						DefaultPagingBar.this.showNextPage();
					}
				});
		this.lastButton_ = new WPushButton(new WString(pbLast), this);
		this.lastButton_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						DefaultPagingBar.this.showLastPage();
					}
				});
		this.view_.pageChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				DefaultPagingBar.this.update();
			}
		});
		this.update();
	}

	private WAbstractItemView view_;
	private WPushButton prevButton_;
	private WPushButton nextButton_;
	private WPushButton firstButton_;
	private WPushButton lastButton_;
	private WText current_;

	private void update() {
		this.firstButton_.setDisabled(this.view_.getCurrentPage() == 0);
		this.prevButton_.setDisabled(this.view_.getCurrentPage() == 0);
		this.nextButton_.setDisabled(this.view_.getCurrentPage() == this.view_
				.getPageCount() - 1);
		this.lastButton_.setDisabled(this.view_.getCurrentPage() == this.view_
				.getPageCount() - 1);
		this.current_
				.setText(new WString("<b>{1}</b> from <b>{2}</b>").arg(
						this.view_.getCurrentPage() + 1).arg(
						this.view_.getPageCount()));
	}

	private void showFirstPage() {
		this.view_.setCurrentPage(0);
	}

	private void showLastPage() {
		this.view_.setCurrentPage(this.view_.getPageCount() - 1);
	}

	private void showPreviousPage() {
		if (this.view_.getCurrentPage() > 0) {
			this.view_.setCurrentPage(this.view_.getCurrentPage() - 1);
		}
	}

	private void showNextPage() {
		if (this.view_.getCurrentPage() < this.view_.getPageCount() - 1) {
			this.view_.setCurrentPage(this.view_.getCurrentPage() + 1);
		}
	}

	private static String pbFirst = "« First";
	private static String pbPrevious = "‹ Previous";
	private static String pbNext = "Next ›";
	private static String pbLast = "Last »";
}
