package me.qsh.newborn.usbconnect.base.usb;

/**
 * usb 通知
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-26 。
 * ============================================================================
 */
public interface UsbControl {

    void notifyUsbConnect(int type);

    void notifyUsbConnectFailed(String errorMsg);

    void notifyUsbDisconnect();

    void notifyUsbWriteSuccess(int num);

    void notifyUsbWriteFailed(String errorMsg);

    void notifyUsbDataReceive(byte[] bytes);

}
