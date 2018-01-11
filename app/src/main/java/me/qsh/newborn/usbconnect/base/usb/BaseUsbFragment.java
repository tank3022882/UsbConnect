package me.qsh.newborn.usbconnect.base.usb;

import me.qsh.newborn.usbconnect.R;
import me.qsh.newborn.usbconnect.app.AppApplication;
import me.qsh.newborn.usbconnect.base.BaseFragment;
import me.qsh.newborn.usbconnect.usb.UsbAccessoryService;
import me.qsh.newborn.usbconnect.usb.UsbFTDeviceService;

/**
 * 基类 USB相关
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-26 。
 * ============================================================================
 */
public abstract class BaseUsbFragment extends BaseFragment implements UsbControl {

    protected int connectType() {
        if (getActivity() instanceof UsbTypeControl) {
            return ((UsbTypeControl) getActivity()).connectType();
        }
        return UsbTypeControl.TYPE_USB_NONE;
    }

    /**
     * 连接成功
     */
    @Override
    public void notifyUsbConnect(int type) {
        AppApplication.showToastShort(R.string.message_usb_connect);
    }

    /**
     * 断开连接
     */
    @Override
    public void notifyUsbDisconnect() {
        AppApplication.showToast(R.string.message_usb_disconnect);
    }

    /**
     * 连接失败
     */
    @Override
    public final void notifyUsbConnectFailed(String errorMsg) {
        AppApplication.showToast(errorMsg);
    }

    /**
     * 发送成功
     *
     * @param num
     */
    @Override
    public void notifyUsbWriteSuccess(int num) {

    }

    /**
     * 发送失败
     *
     * @param errorMsg
     */
    @Override
    public void notifyUsbWriteFailed(String errorMsg) {
        AppApplication.showToast(errorMsg);
    }


    /**
     * 读取成功
     *
     * @param bytes
     */
    @Override
    public void notifyUsbDataReceive(byte[] bytes) {

    }

    protected void sendData(byte[] bytes) {
        int type = connectType();
        switch (type) {
            case UsbTypeControl.TYPE_USB_FTDEVICE:
                UsbFTDeviceService.sendData(getActivity(), bytes);
                break;
            case UsbTypeControl.TYPE_USB_ACCESSORY:
                UsbAccessoryService.sendData(getActivity(), bytes);
                break;
        }
    }

}
