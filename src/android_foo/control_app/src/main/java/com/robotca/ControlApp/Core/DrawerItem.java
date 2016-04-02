package com.robotca.ControlApp.Core;

/**
 * Simple container for information about one of the Drawer items.
 *
 * Created by Michael Brunson on 1/23/16.
 */
public class DrawerItem {
    String itemName;
    int imgResID;

    /**
     * Creates a DrawerItem with the specified name and icom.
     * @param itemName The item's name
     * @param imgResID The image resource id of the item's icon
     */
    public DrawerItem(String itemName, int imgResID) {
        super();
        this.itemName = itemName;
        this.imgResID = imgResID;
    }

    /**
     * @return The item's name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Sets the item's name.
     * @param itemName The new name
     */
    @SuppressWarnings("unused")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * @return The image resource id of the item's icon
     */
    public int getImgResID() {
        return imgResID;
    }

    /**
     * Sets the item's icon.
     * @param imgResID The image resource id of the new icon
     */
    @SuppressWarnings("unused")
    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }
}
