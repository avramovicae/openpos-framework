package org.jumpmind.pos.core.ui.message;

import org.jumpmind.pos.core.screen.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.messagepart.BaconStripPart;

import java.util.ArrayList;
import java.util.List;

public class HomeUIMessage extends UIMessage {
    private BaconStripPart baconStripPart = new BaconStripPart();
    private List<ActionItem> menuItems = new ArrayList<>();

    public List<ActionItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<ActionItem> menuItems) {
        this.menuItems = menuItems;
    }

    public void addMenuItem(ActionItem item) {
        if( this.menuItems == null ) {
            this.menuItems = new ArrayList<>();
        }

        this.menuItems.add(item);
    }

    public BaconStripPart getBaconStripPart() {
        return baconStripPart;
    }

    public void setBaconStripPart(BaconStripPart baconStripPart) {
        this.baconStripPart = baconStripPart;
    }
}