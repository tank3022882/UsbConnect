package me.qsh.newborn.usbconnect.base.usb;

/**
 * usb 类型
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-26 。
 * ============================================================================
 */
public interface UsbTypeControl {

    public static final int TYPE_USB_NONE = 0x00;
    public static final int TYPE_USB_FTDEVICE = 0x01;
    public static final int TYPE_USB_ACCESSORY = 0x02;

    int connectType();

}
