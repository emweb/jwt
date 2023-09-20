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
 *
 * <p><i><b>Note: </b>When using {@link WBootstrap5Theme}, no color schemes are applied by default.
 * You will have to add a &quot;bg-&quot; style class, and &quot;navbar-light&quot; or
 * &quot;navbar-dark&quot; yourself. See the <a
 * href="https://getbootstrap.com/docs/5.1/components/navbar/#color-schemes">Bootstrap documentation
 * on navbar color schemes</a> for more info. </i>
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
    WApplication app = WApplication.getInstance();
    WBootstrap5Theme bs5Theme = ObjectUtils.cast(app.getTheme(), WBootstrap5Theme.class);
    if (bs5Theme != null) {
      this.setResponsive(true);
    }
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
      WApplication app = WApplication.getInstance();
      app.getTheme().apply(this, titleLink, WidgetThemeRole.NavBrand);
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
   *
   * <p>
   *
   * <p><i><b>Note: </b>When using {@link WBootstrap5Theme} the navigation bar is responsive by
   * default. Setting this to false has no effect. You can change the collapsing behavior by setting
   * one of the &quot;.navbar-expand-&quot; style classes, see the <a
   * href="https://getbootstrap.com/docs/5.1/components/navbar/">Bootstrap documentation on
   * navbars</a> for more info. </i>
   */
  public void setResponsive(boolean responsive) {
    NavContainer contents = (NavContainer) this.resolveWidget("contents");
    WApplication app = WApplication.getInstance();
    WBootstrap5Theme bs5Theme = ObjectUtils.cast(app.getTheme(), WBootstrap5Theme.class);
    if (bs5Theme != null) {
      if (!responsive || (WInteractWidget) this.resolveWidget("collapse-button") != null) {
        return;
      }
    }
    if (bs5Theme != null) {
      WInteractWidget collapseButtonPtr = this.getCreateCollapseButton();
      WInteractWidget collapseButton = collapseButtonPtr;
      this.bindWidget("collapse-button", collapseButtonPtr);
      collapseButton
          .clicked()
          .addListener(
              "function(o){let navbarCollapse = o.parentElement.querySelector('.navbar-collapse');if (typeof navbarCollapse === 'null') return;new bootstrap.Collapse(navbarCollapse);}");
      if (!app.getEnvironment().hasAjax()) {
        collapseButton
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  WNavigationBar.this.toggleContents();
                });
      }
      WApplication.getInstance().getTheme().apply(this, contents, WidgetThemeRole.NavCollapse);
    } else {
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
  }
  /**
   * Adds a menu to the navigation bar.
   *
   * <p>Typically, a navigation bar will contain at least one menu which implements the top-level
   * navigation options allowed by the navigation bar.
   *
   * <p>The menu may be aligned to the left or to the right of the navigation bar.
   *
   * <p>
   *
   * <p><i><b>Note: </b>{@link WBootstrap5Theme} ignores alignment. Use classes like
   * &quot;me-auto&quot; and &quot;ms-auto&quot; for alignment instead. </i>
   */
  public WMenu addMenu(WMenu menu, AlignmentFlag alignment) {
    WMenu m = menu;
    this.addWidget(menu, alignment);
    WApplication app = WApplication.getInstance();
    app.getTheme().apply(this, m, WidgetThemeRole.NavbarMenu);
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
   *
   * <p>
   *
   * <p><i><b>Note: </b>{@link WBootstrap5Theme} ignores alignment. Use classes like
   * &quot;me-auto&quot; and &quot;ms-auto&quot; for alignment instead. </i>
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
   *
   * <p>
   *
   * <p><i><b>Note: </b>{@link WBootstrap5Theme} ignores alignment. Use classes like
   * &quot;me-auto&quot; and &quot;ms-auto&quot; for alignment instead. </i>
   */
  public void addSearch(WLineEdit field, AlignmentFlag alignment) {
    WApplication app = WApplication.getInstance();
    app.getTheme().apply(this, field, WidgetThemeRole.NavbarSearchInput);
    this.addWrapped(field, alignment, WidgetThemeRole.NavbarSearchForm);
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
   *
   * <p>
   *
   * <p><i><b>Note: </b>{@link WBootstrap5Theme} ignores alignment. Use classes like
   * &quot;me-auto&quot; and &quot;ms-auto&quot; for alignment instead. </i>
   */
  public void addWidget(WWidget widget, AlignmentFlag alignment) {
    if (ObjectUtils.cast(widget, WMenu.class) != null) {
      this.align(widget, alignment);
      WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
      contents.addWidget(widget);
      contents.setLoadLaterWhenInvisible(false);
    } else {
      this.addWrapped(widget, alignment, WidgetThemeRole.NavbarForm);
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
    WApplication app = WApplication.getInstance();
    app.getTheme().apply(this, result, WidgetThemeRole.NavbarBtn);
    return result;
  }

  private void toggleContents() {
    WApplication app = WApplication.getInstance();
    if (app.getEnvironment().hasAjax()) {
      return;
    }
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WInteractWidget collapseButton = (WInteractWidget) this.resolveWidget("collapse-button");
    if (contents.hasStyleClass("show")) {
      contents.removeStyleClass("show");
      collapseButton.addStyleClass("collapsed");
    } else {
      contents.addStyleClass("show");
      collapseButton.removeStyleClass("collapsed");
    }
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

  private void addWrapped(WWidget widget, AlignmentFlag alignment, int role) {
    WContainerWidget contents = (WContainerWidget) this.resolveWidget("contents");
    WContainerWidget wrap = new WContainerWidget((WContainerWidget) contents);
    WApplication app = WApplication.getInstance();
    app.getTheme().apply(this, wrap, role);
    this.align(wrap, alignment);
    wrap.addWidget(widget);
  }

  private void align(WWidget widget, AlignmentFlag alignment) {
    WApplication app = WApplication.getInstance();
    switch (alignment) {
      case Left:
        app.getTheme().apply(this, widget, WidgetThemeRole.NavbarAlignLeft);
        break;
      case Right:
        app.getTheme().apply(this, widget, WidgetThemeRole.NavbarAlignRight);
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
