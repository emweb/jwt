/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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

class DialogCover extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(DialogCover.class);

  public DialogCover() {
    super();
    this.dialogs_ = new ArrayList<WDialog>();
    this.topDialogId_ = "";
    this.setObjectName("dialog-cover");
    this.hide();
  }

  public void pushDialog(WDialog dialog, final WAnimation animation) {
    this.dialogs_.add(dialog);
    if (dialog.isModal()) {
      this.coverFor(dialog, animation);
    }
    this.scheduleRender();
  }

  public void popDialog(WDialog dialog, final WAnimation animation) {
    this.dialogs_.remove(dialog);
    WDialog topModal = null;
    for (int i = this.dialogs_.size(); i > 0; --i) {
      int j = i - 1;
      if (this.dialogs_.get(j).isModal()) {
        topModal = this.dialogs_.get(j);
        break;
      }
    }
    this.coverFor(topModal, animation);
    if (this.dialogs_.isEmpty()) {
      if (this != null) this.remove();
    } else {
      this.scheduleRender();
    }
  }

  public boolean isExposed(WWidget w) {
    for (int i = this.dialogs_.size(); i > 0; --i) {
      int j = i - 1;
      if (this.dialogs_.get(j).isExposed(w)) {
        return true;
      }
      if (this.dialogs_.get(j).isModal()) {
        return this.isInOtherPopup(w);
      }
    }
    return false;
  }

  public boolean isTopDialogRendered(WDialog dialog) {
    return dialog.getId().equals(this.topDialogId_);
  }

  public void bringToFront(WDialog dialog) {
    if (this.dialogs_.remove(dialog)) {
      this.dialogs_.add(dialog);
      this.scheduleRender();
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.dialogs_.isEmpty()) {
      this.topDialogId_ = "";
    } else {
      this.topDialogId_ = this.dialogs_.get(this.dialogs_.size() - 1).getId();
    }
  }

  private List<WDialog> dialogs_;
  private String topDialogId_;

  private void coverFor(WDialog dialog, final WAnimation animation) {
    if (dialog != null) {
      if (this.isHidden()) {
        if (!animation.isEmpty()) {
          this.animateShow(
              new WAnimation(
                  AnimationEffect.Fade, TimingFunction.Linear, animation.getDuration() * 4));
        } else {
          this.show();
        }
        WApplication.getInstance().pushExposedConstraint(this);
      }
      dialog.doJSAfterLoad(
          "setTimeout(function() {"
              + WApplication.getInstance().getJavaScriptClass()
              + "._p_.updateGlobal('"
              + dialog.layoutContainer_.getId()
              + "') }, 0);");
      this.setZIndex(dialog.getZIndex() - 1);
      this.setStyleClass(this.userCoverClasses(dialog));
      WApplication app = WApplication.getInstance();
      app.getTheme().apply(app.getDomRoot(), this, WidgetThemeRole.DialogCoverWidget);
    } else {
      WApplication.getInstance()
          .doJavaScript(
              "setTimeout(function() {"
                  + WApplication.getInstance().getJavaScriptClass()
                  + "._p_.updateGlobal(null) });");
      if (!this.isHidden()) {
        if (!animation.isEmpty()) {
          this.animateHide(
              new WAnimation(
                  AnimationEffect.Fade, TimingFunction.Linear, animation.getDuration() * 4));
        } else {
          this.hide();
        }
        WApplication.getInstance().popExposedConstraint(this);
      }
    }
  }

  private String userCoverClasses(WWidget w) {
    String c = w.getStyleClass();
    List<String> classes = new ArrayList<String>();
    StringUtils.split(classes, c, " ", false);
    String result = "";
    for (int i = 0; i < classes.size(); ++i) {
      if (classes.get(i).length() != 0 && !classes.get(i).startsWith("Wt-")) {
        if (result.length() != 0) {
          result += " ";
        }
        result += classes.get(i) + "-cover";
      }
    }
    return result;
  }

  private boolean isInOtherPopup(WWidget w) {
    WApplication app = WApplication.getInstance();
    for (WWidget p = w; p != null; p = p.getParent()) {
      if (ObjectUtils.cast(p, WDialog.class) != null) {
        return false;
      }
      if (p == app.getDomRoot()) {
        return w != app.getRoot();
      }
      w = p;
    }
    return false;
  }
}
