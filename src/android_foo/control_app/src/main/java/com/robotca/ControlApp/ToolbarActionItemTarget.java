package com.robotca.ControlApp;

import android.graphics.Point;
import android.support.annotation.IdRes;
import android.support.v7.widget.Toolbar;

import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

/**
 * Container for a Toolbar Action Item.
 *
 * Created by Michael Brunson on 3/9/16.
 */
public class ToolbarActionItemTarget implements Target {
    private final Toolbar toolbar;
    private final int menuItemId;

    /**
     * Creates a ToolbarActionItemTarget.
     * @param toolbar The parent Toolbar
     * @param itemId The image resource item id for the icon
     */
    public ToolbarActionItemTarget(Toolbar toolbar, @IdRes int itemId) {
        this.toolbar = toolbar;
        this.menuItemId = itemId;
    }

    @Override
    public Point getPoint() {
        return new ViewTarget(toolbar.findViewById(menuItemId)).getPoint();
    }
}
