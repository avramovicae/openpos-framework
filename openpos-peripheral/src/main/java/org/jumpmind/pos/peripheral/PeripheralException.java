package org.jumpmind.pos.peripheral;

public class PeripheralException extends RuntimeException {

    public PeripheralException() {
        super();
    }

    public PeripheralException(String message) {
        super(message);
    }

    public PeripheralException(String message, Throwable cause) {
        super(message, cause);
    }

    public PeripheralException(Throwable cause) {
        super(cause);
    }
}
