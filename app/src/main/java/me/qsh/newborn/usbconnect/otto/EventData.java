package me.qsh.newborn.usbconnect.otto;

/**
 * 事件
 * ============================================================================
 * 版权所有 2018 。
 *
 * @author fallenpanda
 * @version 1.0 2018-01-10 。
 * ============================================================================
 */
public class EventData {

    public final static int EVENT_USB_CONNECT = 0x01;
    public final static int EVENT_USB_CONNECT_FAIL = 0x02;
    public final static int EVENT_USB_DISCONNECT = 0x03;
    public final static int EVENT_USB_WRITE_SUCCESS = 0x04;
    public final static int EVENT_USB_WRITE_FAIL = 0x05;
    public final static int EVENT_USB_RECEIVE = 0x06;

    public EventData(int eventType) {
        this.eventType = eventType;
    }

    public EventData(int eventType, Object eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public int eventType;
    public Object eventData;

}
