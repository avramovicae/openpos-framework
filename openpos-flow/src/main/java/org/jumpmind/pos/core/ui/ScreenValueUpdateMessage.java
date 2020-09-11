package org.jumpmind.pos.core.ui;

import lombok.Data;
import org.jumpmind.pos.util.model.Message;

@Data
public class ScreenValueUpdateMessage extends Message {

    public ScreenValueUpdateMessage() {
        super("ScreenValueUpdate");
    }

    private String valuePath;
    private Object value;

}
