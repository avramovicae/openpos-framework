package org.jumpmind.pos.peripheral.scale;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.peripheral.PeripheralException;
import org.jumpmind.pos.print.*;
import org.jumpmind.pos.util.ClassUtils;
import org.jumpmind.pos.util.ReflectionException;
import org.jumpmind.pos.util.status.IStatusManager;
import org.jumpmind.pos.util.status.IStatusReporter;
import org.jumpmind.pos.util.status.Status;
import org.jumpmind.pos.util.status.StatusReport;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class CheckoutScale implements IStatusReporter {

    static final int SCALE_RESPONSE_BYTE = 0b0000010;
    static final int SCALE_ERROR_BYTE = '?';

    static final int STATUS_SCALE_IN_MOTION             = 0b0000001;
    static final int STATUS_OVER_CAPACITY               = 0b0000010;
    static final int STATUS_UNDER_0                     = 0b0000100;
    static final int STATUS_OUTSIDE_ZERO_CAPTURE_RANGE  = 0b0001000;
    static final int STATUS_CENTER_OF_0                 = 0b0010000;
    static final int STATUS_NET_WEIGHT                  = 0b0100000;
    static final int STATUS_BAD_COMMAND                 = 0b1000000;

    public final static String STATUS_NAME = "DEVICE.CHECKOUT_SCALE";
    public final static String STATUS_ICON = "pole-display";

    Map<String, Object> settings;
    PeripheralConnection peripheralConnection;
    IConnectionFactory connectionFactory;

    IStatusManager statusManager;

    public void open(Map<String,Object> settings) {
        this.settings = settings;
        try {
            String className = (String)this.settings.get("connectionClass");
            if (StringUtils.isEmpty(className)) {
                throw new ReflectionException("The connectionClass setting is required for the checkout scale, but was not provided.");
            }
            this.connectionFactory = ClassUtils.instantiate(className);
        } catch (Exception ex) {
            if (statusManager != null) {
                statusManager.reportStatus(new StatusReport(STATUS_NAME, STATUS_ICON, Status.Offline, ex.getMessage()));
            }
            throw new PeripheralException("Failed to create the connection factory for " + getClass().getName(), ex);
        }
        log.info("Opening checkout scale with settings: " + this.settings);

        this.peripheralConnection = connectionFactory.open(this.settings);

        log.info("Checkout scale appears to be successfully opened.");

        performConfidenceTest();

        ScaleWeightData weight = getScaleWeightData();
        System.out.println(weight.getWeight());
    }

    public ScaleWeightData getScaleWeightData() {
        byte[] response = sendScaleCommand((byte)'W');
        if (response.length < 2) {
            throw new PeripheralException("Unrecognized response from checkout scale " + Arrays.toString(response));
        }

        if (response[1] == SCALE_ERROR_BYTE) {
            String status = "";
            byte statusByte = response[2];
            if (statusByte > 0) {
                status = checkStatus(statusByte);

            } else {
                performConfidenceTest();
            }

            throw new PeripheralException("Failed to read checkout scale weight. " + status);
        }


        // TODO here assemble string to convert to bigdecimal

        StringBuilder weightString = new StringBuilder();
        for (int i = 1; i < response.length; i++) {
            weightString.append((char)response[i]);
        }

        BigDecimal weight;
        try {
            weight = new BigDecimal(weightString.toString());
        } catch (Exception ex) {
            throw new PeripheralException("failed to convert scale weight to decimal: '" + weightString + "'", ex);
        }


        ScaleWeightData scaleWeightData = new ScaleWeightData();
        scaleWeightData.setWeight(weight);
        return scaleWeightData;
    }

    protected void performConfidenceTest() {
        sendScaleCommand((byte)'A');
        byte[] response = sendScaleCommand((byte)'B');

        if (response.length < 3) {
            throw new PeripheralException("Unrecognized confidence response from checkout scale " + Arrays.toString(response));
        }

        final int STATUS_BYTE_POSITION = 2;

        byte statusByte = response[STATUS_BYTE_POSITION];
        if (statusByte == '@') {
            log.info("Successful checkout scale confidence test.");
            return;
        } else {
            throw new PeripheralException("Failed checkout scale confidence test with status " + statusByte);
        }
    }

    protected byte[] sendScaleCommand(byte command) {
        try {
            this.peripheralConnection.getOut().write(command);
            Thread.sleep(200);

            List<Integer> bytes = new ArrayList<Integer>();

            int responseByte = -1;

            while ((responseByte = this.peripheralConnection.getIn().read()) != -1) {
                if (responseByte != '\r' && responseByte != '\n') { // most scale commands terminate with \r\n
                    bytes.add(responseByte);
                }
            }

            byte[] response = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); i++) {
                response[i] = bytes.get(i).byteValue();
            }

            if (response.length == 0 || response[0] != SCALE_RESPONSE_BYTE) {
                throw new PeripheralException("Unrecognized response from checkout scale. Should start with byte " +
                        SCALE_RESPONSE_BYTE + " but was " + Arrays.toString(response));
            }

            return response;

        } catch (Exception ex) {
            if (ex instanceof PeripheralException) {
                throw (PeripheralException)ex;
            }
            throw new PeripheralException("Failed to send scale command " + command, ex);
        }

    }

    protected String checkStatus(int statusByte) {

        StringBuilder buff = new StringBuilder();

        buff.append("Checkout Scale Status: ");

        System.out.println();
        if ((statusByte & STATUS_NET_WEIGHT) > 0) {
            buff.append("(net weight)");
        }
        if ((statusByte & STATUS_SCALE_IN_MOTION) > 0) {
            buff.append("(The scale in motion)");
        }
        if ((statusByte & STATUS_OVER_CAPACITY) > 0) {
            buff.append("(The Scale is over capacity)");
        }
        if ((statusByte & STATUS_UNDER_0) > 0) {
            buff.append("(The scale is reading under 0)");
        }
        if ((statusByte & STATUS_OUTSIDE_ZERO_CAPTURE_RANGE) > 0) {
            buff.append("(The scale outside zero capture range)");
        }
        if ((statusByte & STATUS_BAD_COMMAND) > 0) {
//            buff.append("[Bad command]");
        }
        if ((statusByte & STATUS_CENTER_OF_0) > 0) {
            buff.append("(Scale center of 0)");
        }

        buff.append(" 0b").append(Integer.toBinaryString(statusByte));

        return buff.toString();


    }


    @Override
    public StatusReport getStatus(IStatusManager statusManager) {
        StatusReport statusReport = new StatusReport();
        statusReport.setStatus(Status.Disabled);
        return statusReport;
    }

    public static void main(String[] args) {
        CheckoutScale scale = new CheckoutScale();
        Map<String, Object> settings = new HashMap<>();

        settings.put("connectionClass", RS232ConnectionFactory.class.getName());
        settings.put("portName", "COM6");
        scale.open(settings);
    }
}
