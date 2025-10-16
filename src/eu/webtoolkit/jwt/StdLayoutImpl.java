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

/**
 * An abstract base class for implementing layout managers.
 *
 * <p>
 *
 * @see StdLayoutItemImpl
 * @see WLayout
 */
public abstract class StdLayoutImpl extends StdLayoutItemImpl implements WLayoutImpl {
  private static Logger logger = LoggerFactory.getLogger(StdLayoutImpl.class);

  /**
   * Constructor.
   *
   * <p>Creates a new {@link StdLayoutImpl} for the given {@link WLayout}.
   */
  public StdLayoutImpl(WLayout layout) {
    super();
    this.layout_ = layout;
  }
  /**
   * Updates the DomElements in the {@link WLayout}.
   *
   * <p>This function should update the DomElements in the {@link WLayout}. This means creating
   * DomElements for newly added {@link StdLayoutItemImpl}, deleting DomElements from deleted {@link
   * StdLayoutItemImpl} and updating the placement and size of the DomElements.
   */
  public abstract void updateDom(final DomElement parent);
  /**
   * Called when a {@link WLayoutItem} in the {@link WLayout} is resized.
   *
   * <p>When a {@link WLayoutItem} is resized, it may be necessary to update the whole layout. Items
   * may have moved in such a way, that it would push other items of screen, requiring other items
   * to adapt to it.
   *
   * <p>If it returns <code>true</code>, a subsequent {@link StdLayoutImpl#updateDom(DomElement
   * parent) updateDom()} may be necessary.
   *
   * <p>
   */
  public abstract boolean itemResized(WLayoutItem item);
  /**
   * Called when the parent is resized.
   *
   * <p>When the parent is resized, it may be necessary to update the whole layout. More or less
   * items could not potentially fit in the layout, or the layout&apos;s boundaries may have
   * changed.
   *
   * <p>If it returns <code>true</code>, a subsequent {@link StdLayoutImpl#updateDom(DomElement
   * parent) updateDom()} may be necessary.
   *
   * <p>
   *
   * @see StdLayoutImpl#updateDom(DomElement parent)
   */
  public abstract boolean isParentResized();
  /** Returns the {@link WLayout} as a {@link WLayoutItem}. */
  public WLayoutItem getLayoutItem() {
    return this.layout_;
  }
  /**
   * Updates the layout.
   *
   * <p>By default, this will trigger a call to {@link StdLayoutImpl#updateDom(DomElement parent)
   * updateDom()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Several calls to {@link StdLayoutImpl#update() update()} may happens before
   * {@link StdLayoutImpl#updateDom(DomElement parent) updateDom()} is called. </i>
   *
   * @see StdLayoutImpl#updateDom(DomElement parent)
   */
  public void update() {
    WContainerWidget c = this.getContainer();
    if (c != null) {
      c.layoutChanged(false);
    }
  }

  public void setObjectName(final String name) {}
  /** Returns the {@link WLayout}. */
  protected WLayout getLayout() {
    return this.layout_;
  }
  /**
   * Returns a {@link WLayoutItem} implementation.
   *
   * <p>Returns a {@link WLayoutItem} implementation if the implementation is a subclass of {@link
   * StdLayoutItemImpl}. Otherwise returns <code>null</code>.
   */
  protected static StdLayoutItemImpl getImpl(WLayoutItem item) {
    return ObjectUtils.cast(item.getImpl(), StdLayoutItemImpl.class);
  }

  private WLayout layout_;
}
