package org.jumpmind.pos.devices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.NOT_FOUND)
public class DeviceNotFoundException extends DevicesException {

    private static final long serialVersionUID = 1L;

    public DeviceNotFoundException() {
        super("A device for this request could not be found");
    }

    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(Throwable cause) {
        super(cause);
    }
}
