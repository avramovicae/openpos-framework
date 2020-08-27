package org.jumpmind.pos.peripheral.scale;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class ScaleWeightData {

    public static enum CheckoutScaleUnit {
        LB,
        KG
    }

    private BigDecimal weight;
    private CheckoutScaleUnit checkoutScaleUnit = CheckoutScaleUnit.LB; // scale under development is hardwired for lb

}
