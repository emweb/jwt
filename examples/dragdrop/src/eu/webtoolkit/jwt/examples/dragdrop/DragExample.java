/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.dragdrop;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WImage;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WText;

/**
 * Class demonstrating drag and drop in JWt
 * 
 * @author plibin0
 */
public class DragExample extends WContainerWidget {
    public DragExample(WContainerWidget parent) {
        super(parent);
        new WText(
                "<p>Help these people with their decision by dragging one of the pills.</p>",
                this);

        if (!WApplication.getInstance().getEnvironment().hasJavaScript()) {
            new WText(
                    "<i>This examples requires that javascript support is enabled.</i>",
                    this);
        }

        WContainerWidget pills = new WContainerWidget(this);
        pills.setContentAlignment(AlignmentFlag.Center);

        createDragImage("icons/blue-pill.jpg", "icons/blue-pill-small.png",
                "blue-pill", pills);
        createDragImage("icons/red-pill.jpg", "icons/red-pill-small.png",
                "red-pill", pills);

        WContainerWidget dropSites = new WContainerWidget(this);

        new Character("Neo", dropSites);
        new Character("Morpheus", dropSites);
        new Character("Trinity", dropSites);
    }

    /**
     * Create an image which can be dragged.
     * 
     * The image to be displayed when dragging is given by smallurl, and
     * configured with the given mime type
     */
    private WImage createDragImage(String url, String smallurl,
            String mimeType, WContainerWidget p) {
        WImage result = new WImage(new WLink(url), p);
        WImage dragImage = new WImage(new WLink(smallurl), p);

        /*
         * Set the image to be draggable, showing the other image (dragImage) to
         * be used as the widget that is visually dragged.
         */
        result.setDraggable(mimeType, dragImage, true);

        return result;
    }
}
