package org.jumpmind.pos.peripheral.scale;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.peripheral.PeripheralException;
import org.jumpmind.pos.print.IConnectionFactory;
import org.jumpmind.pos.print.PeripheralConnection;
import org.jumpmind.pos.print.PrintException;
import org.jumpmind.pos.print.UsbConnectionFactory;
import org.jumpmind.pos.util.ClassUtils;
import org.jumpmind.pos.util.ReflectionException;
import org.jumpmind.pos.util.status.IStatusManager;
import org.jumpmind.pos.util.status.IStatusReporter;
import org.jumpmind.pos.util.status.Status;
import org.jumpmind.pos.util.status.StatusReport;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CheckoutScale implements IStatusReporter {

    public final static String STATUS_NAME = "DEVICE.POLE_DISPLAY";
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
                throw new ReflectionException("The connectionClass setting is required for the pole display, but was not provided.");
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
//        showText("Starting");
        log.info("Checkout scale appears to be successfully opened.");

        try {
            while (true) {
                this.peripheralConnection.getOut().write((byte)'W');
                Thread.sleep(200);

                int lastByte = -1;

                while ((lastByte = this.peripheralConnection.getIn().read()) != -1) {
                    System.out.println(lastByte);
                }

                Thread.sleep(2000);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

        settings.put("connectionClass", UsbConnectionFactory.class.getName());
        settings.put("usbVendorId", 0x0eb8);
        settings.put("usbDeviceId", "*");
        scale.open(settings);
    }
}
