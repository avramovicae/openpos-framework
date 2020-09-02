package org.jumpmind.pos.peripheral.scale;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class ScaleWeightData {

    public static enum CheckoutScaleUnit {
        LB,
        KG
    }
    public static enum CheckoutScaleFailureCode {
        UNSPECIFIED,
        SCALE_IN_MOTION,
        SCALE_OVER_CAPACITY,
        SCALE_READ_UNDER_0
    }

    private boolean sucessful;
    private CheckoutScaleFailureCode failureCode = CheckoutScaleFailureCode.UNSPECIFIED;
    private String failureMessage;

    private BigDecimal weight;
    private CheckoutScaleUnit checkoutScaleUnit = CheckoutScaleUnit.LB; // scale under development is hardwired for lb

}
