/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A navigation bar.
 *
 * <p>
 *
 * <p><i><b>Note: </b>{@link WNavigationBar} is currently only styled in the Bootstrap themes. </i>
 */
public class WNavigationBar extends WTemplate {
  private static Logger logger = LoggerFactory.getLogger(WNavigationBar.class);

  /** Constructor. */
  public WNavigationBar(WContainerWidget parentContainer) {
    super(tr("Wt.WNavigationBar.template"), (WContainerWidget) null);
    this.bindEmpty("collapse-button");
    this.bindEmpty("expand-button");
    this.bindEmpty("title-link");
    this.bindWidget("contents", new NavContainer());
    // this.implementStateless(WNavigationBar.collapseContents,WNavigationBar.undoExpandContents);
    // this.implementStateless(WNavigationBar.expandContents,WNavigationBar.undoExpandContents);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WNavigationBar(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WNavigationBar() {
    this((WContainerWidget) null);
  }
  /**
   * Sets a title.
   *
   * <p>The title may optionally link to a &apos;homepage&apos;.
   */
  public void setTitle(final CharSequence title, final WLink link) {
    WAnchor titleLink = (WAnchor) this.resolveWidget("title-link");
    if (!(titleLink != null)) {
      titleLink = new WAnchor();
      this.bindWidget("title-link", titleLink);
      WApplication.getInstance().getTheme().apply(this, titleLink, WidgetThemeRole.NavBrand);
    }
    titleLink.setText(title);
    titleLink.setLink(link);
  }
  /**
   * Sets a title.
   *
   * <p>Calls {@link #setTitle(CharSequence title, WLink link) setTitle(title, new WLink())}
   */
  public final void setTitle(final CharSequence title) {
    setTitle(title, new WLink());
  }
  /**
   * Sets whether the navigation bar will respond to screen size.
   *
   * <p>For screens that are less wide, the navigation bar can be rendered different (more compact
   * and allowing for vertical menu layouts).
   */
  public void setResponsive(boolean responsive) {
    NavContainer contents = (NavContainer) this.resolveWidget("contents");
    if (responsive) {
      WInteractWidget collapseButton = (WInteractWidget) this.resolveWidget("collapse-button");
      WInteractWidget expandButton = (WInteractWidget) this.resolveWidget("expand-button");
      if (!(collapseButton != null)) {
        WInteractWidget b = this.getCreateCollapseButton();
        collapseButton = b;
        this.bindWidget("collapse-button", b);
        collapseButton
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  WNavigationBar.this.collapseContents();
                });
        collapseButton.hide();
        b = this.getCreateExpandButton();
        expandButton = b;
        this.bindWidget("expand-button", b);
        expandButton
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  WNavigationBar.this.expandContents();
                });
      }
      WApplication.getInstance().getTheme().apply(this, contents, WidgetThemeRole.NavCollapse);
      contents.hide();
      if (contents.isBootstrap2Responsive()) {
        contents.setJavaScriptMember(
            "wtAnimatedHidden",
            "function(hidden) {if (hidden) this.style.height=''; this.style.display='';}");
      }
    } else {
      this.bindEmpty("collapse-button");
    }
  }
  /**
   * Adds a menu to the navigation bar.
   *
   * <p>Typically, a navigation bar will contain at least one menu which implements the top-level
   * navigation options allowed by the navigation bar.
   *
   * <p>The menu may be aligned to the left or to the right of the navigation bar.
   */
  public WMenu addMenu(WMenu menu, AlignmentFlag alignment) {
    WMenu m = menu;
    this.addWidget(menu, alignment);
    WApplication.getInstance().getTheme().apply(this, m, WidgetThemeRole.NavbarMenu);
    return m;
  }
  /**
   * Adds a menu to the navigation bar.
   *
   * <p>Returns {@link #addMenu(WMenu menu, AlignmentFlag alignment) addMenu(menu,
   * AlignmentFlag.Left)}
   */
  public final WMenu addMenu(WMenu menu) {
    return addMenu(menu, AlignmentFlag.Left);
  }
  /**
   * Adds a form field to the navigation bar.
   *
   * <p>In some cases, one may want to add a few form fields to the navigation bar (e.g. for a
   * compact login option).
   */
  public void addFormField(WWidget widget, AlignmentFlag alignment) {
    this.addWidget(widget, alignment);
  }
  /**
   * Adds a form field to the navigation bar.
   *
   * <p>Calls {@link #addFormField(WWidget widget, AlignmentFlag alignment) addFormField(widget,
   * AlignmentFlag.Left)}
   */
  public final void addFormField(WWidget widget) {
    addFormField(widget, AlignmentFlag.Left);
  }
  /**
   * Adds a search widget to the navigation bar.
   *
   * <p>This is not so different from {@link WNavigationBar#addFormField(WWidget widget,
   * AlignmentFlag alignment) addFormField()}, except that the form field may be styled differently
   * to indicate a search function.
   */
  public void addSearch(WLineEdit field, AlignmentFlag alignment) {
    WApplication.getInstance().getTheme().apply(this, field, WidgetThemeRole.NavbarSearch);
    this.addWrapped(field, alignment, "navbar-form");
  }
  /**
   * Adds a search widget to the navigation bar.
   *
   * <p>Calls {@link #addSearch(WLineEdit field, AlignmentFlag alignment) addSearch(field,
   * AlignmentFlag.Left)}
   */
  public final void addSearch(WLineEdit field) {
    addSearch(field, AlignmentFlag.Left);
  }
  /**
   * Adds a widget to the navigation bar.
   *
   * <p>Any other widget may be added to the navigation bar, although they may require special CSS
   * style to blend well with the navigation bar style.
   */
  public void addWidget(WWidget widget, AlignmentFlag alignment) {
    if (((widget) instanceof WMenu ? (WMenu) (widget) : null) != null) {
      this.align(widget, alignment);
      WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
      contents.addWidget(widget);
      contents.setLoadLaterWhenInvisible(false);
    } else {
      this.addWrapped(widget, alignment, "navbar-form");
    }
  }
  /**
   * Adds a widget to the navigation bar.
   *
   * <p>Calls {@link #addWidget(WWidget widget, AlignmentFlag alignment) addWidget(widget,
   * AlignmentFlag.Left)}
   */
  public final void addWidget(WWidget widget) {
    addWidget(widget, AlignmentFlag.Left);
  }
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget) ;
  protected WInteractWidget getCreateCollapseButton() {
    return this.getCreateExpandButton();
  }

  protected WInteractWidget getCreateExpandButton() {
    WPushButton result =
        new WPushButton(tr("Wt.WNavigationBar.expand-button"), (WContainerWidget) null);
    result.setTextFormat(TextFormat.XHTML);
    WApplication.getInstance().getTheme().apply(this, result, WidgetThemeRole.NavbarBtn);
    return result;
  }

  private void expandContents() {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WInteractWidget collapseButton = (WInteractWidget) this.resolveWidget("collapse-button");
    WInteractWidget expandButton = (WInteractWidget) this.resolveWidget("expand-button");
    collapseButton.show();
    expandButton.hide();
    if (!this.isAnimatedResponsive()) {
      contents.show();
    } else {
      contents.animateShow(new WAnimation(AnimationEffect.SlideInFromTop, TimingFunction.Ease));
    }
  }

  private void collapseContents() {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WInteractWidget collapseButton = (WInteractWidget) this.resolveWidget("collapse-button");
    WInteractWidget expandButton = (WInteractWidget) this.resolveWidget("expand-button");
    collapseButton.hide();
    expandButton.show();
    if (!this.isAnimatedResponsive()) {
      contents.hide();
    } else {
      contents.animateHide(new WAnimation(AnimationEffect.SlideInFromTop, TimingFunction.Ease));
    }
  }

  private void undoExpandContents() {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WInteractWidget collapseButton = (WInteractWidget) this.resolveWidget("collapse-button");
    WInteractWidget expandButton = (WInteractWidget) this.resolveWidget("expand-button");
    collapseButton.hide();
    expandButton.show();
    if (!this.isAnimatedResponsive()) {
      contents.hide();
    } else {
      contents.show();
    }
  }

  private void addWrapped(WWidget widget, AlignmentFlag alignment, String wrapClass) {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WContainerWidget wrap = new WContainerWidget();
    contents.addWidget(wrap);
    wrap.setStyleClass(wrapClass);
    this.align(wrap, alignment);
    wrap.addWidget(widget);
  }

  private void addWrapped(WWidget widget, WWidget parent, int role, AlignmentFlag alignment) {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WContainerWidget wrap = new WContainerWidget();
    contents.addWidget(wrap);
    WApplication.getInstance().getTheme().apply(widget, parent, role);
    this.align(wrap, alignment);
    wrap.addWidget(widget);
  }

  private void align(WWidget widget, AlignmentFlag alignment) {
    switch (alignment) {
      case Left:
        WApplication.getInstance().getTheme().apply(this, widget, WidgetThemeRole.NavbarAlignLeft);
        break;
      case Right:
        WApplication.getInstance().getTheme().apply(this, widget, WidgetThemeRole.NavbarAlignRight);
        break;
      default:
        logger.error(
            new StringWriter()
                .append("addWidget(...): unsupported alignment ")
                .append(String.valueOf(alignment.getValue()))
                .toString());
    }
  }

  private boolean isAnimatedResponsive() {
    return WApplication.getInstance().getEnvironment().supportsCss3Animations()
        && WApplication.getInstance().getEnvironment().hasAjax();
  }
}
