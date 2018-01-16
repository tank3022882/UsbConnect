package me.qsh.newborn.usbconnect.usb;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.orhanobut.logger.Logger;

import me.qsh.newborn.usbconnect.base.usb.UsbTypeControl;
import me.qsh.newborn.usbconnect.otto.EventData;
import me.qsh.newborn.usbconnect.otto.OttoProvider;
import me.qsh.newborn.usbconnect.utils.ByteUtils;

/**
 * usb FT Device
 * ============================================================================
 * 版权所有 2018 。
 *
 * @author fallenpanda
 * @version 1.0 2018-01-11 。
 * ============================================================================
 */
public class UsbFTDeviceService extends Service {

    public final static String ACTION_USB_START = "me.qsh.newborn.usbconnect.action.USB_START";
    public final static String ACTION_USB_SEND_DATA = "me.qsh.newborn.usbconnect.action.USB_WRITE";
    public final static String ACTION_EXIT = "me.qsh.newborn.usbconnect.action.EXIT";
    public final static String SEND_DATA = "SEND_DATA";

    // config
    private final int baudRate = 57600;
    private final byte dataBits = D2xxManager.FT_DATA_BITS_8;
    private final byte stopBits = D2xxManager.FT_STOP_BITS_1;
    private final byte parity = D2xxManager.FT_PARITY_NONE;
    private final short flowControl = D2xxManager.FT_FLOW_NONE;
    private final byte xon = 0;
    private final byte xoff = 0;
    private final boolean dtr = false;
    private final boolean rts = false;
    private final int readSize = 512;

    private D2xxManager mUsbManager;
    private FT_Device mDevice;

    private ReadThread readThread;
    private volatile boolean readThreadGoing = false;

    private final BroadcastReceiver mDetachedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                stopSelf();
            }
        }
    };

    public static void startDevice(Context context) {
        Intent intent = new Intent(context, UsbFTDeviceService.class);
        intent.setAction(ACTION_USB_START);
        context.startService(intent);
    }

    public static void exitService(Context context) {
        Intent intent = new Intent(context, UsbFTDeviceService.class);
        intent.setAction(ACTION_EXIT);
        context.startService(intent);
    }

    public static void sendData(Context context, byte[] bytes) {
        Intent intent = new Intent(context, UsbFTDeviceService.class);
        intent.setAction(ACTION_USB_SEND_DATA);
        intent.putExtra(SEND_DATA, bytes);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("onCreate()");
        try {
            this.mUsbManager = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException e) {
            Logger.e(e, "D2xxManager.getInstance(this)");
        }
        registerReceiver(mDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            Logger.d("onStartCommand() -> "+ action);
            if (ACTION_USB_START.equals(action)) {// 启动
                openDevice();
            } else if (ACTION_USB_SEND_DATA.equals(action)) {// 发送数据
                if (isOpen()) {
                    byte[] bytes = startIntent.getByteArrayExtra(SEND_DATA);
                    writeData(bytes);
                } else {
                    stopSelf();
                }
            }else if (ACTION_EXIT.equals(action)) {
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Logger.d("onDestroy()");
        closeAccessory();
        unregisterReceiver(mDetachedReceiver);
        super.onDestroy();
    }

    private boolean isOpen() {
        return mDevice!=null && mDevice.isOpen();
    }

    /**
     * 设置
     */
    private void setConfig() {
        Logger.d("setConfig()");
        if (isOpen()) {
            // 重置
            mDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
            // 设置 波特率
            mDevice.setBaudRate(baudRate);
            // 设置 数据属性
            mDevice.setDataCharacteristics(dataBits, stopBits, parity);
            // 设置 流控制
            mDevice.setFlowControl(flowControl, xon, xoff);
            // DTR
            if (dtr) {
                mDevice.setDtr();
            } else {
                mDevice.clrDtr();
            }
            // RTS
            if (rts) {
                mDevice.setRts();
            } else {
                mDevice.clrRts();
            }
        }
    }

    /**
     * 写入
     *
     * @param data
     */
    private void writeData(byte[] data) {
        if (data == null) data = new byte[0];
        StringBuffer str = new StringBuffer("");
        for (byte b: data) {
            str.append(ByteUtils.byteToInt(b)+" ");
        }
        Logger.d(str);
        if (isOpen()) {
            int length = mDevice.write(data);
            OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_WRITE_SUCCESS, length));
        } else {
            OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_WRITE_FAIL, "write fail: accessory not open"));
        }
    }

    private void openDevice() {
        Logger.d("openDevice()");
        int devCount = mUsbManager.createDeviceInfoList(this);
        if (devCount > 0) {
            if (null == mDevice) {
                mDevice = mUsbManager.openByIndex(this, 0);
            } else {
                synchronized (mDevice) {
                    mDevice = mUsbManager.openByIndex(this, 0);
                }
            }
            if (isOpen()) {
                setConfig();
                if (!readThreadGoing) {
                    readThreadGoing = true;

                    readThread = new ReadThread();
                    readThread.start();
                }
                OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT, UsbTypeControl.TYPE_USB_FTDEVICE));
            } else {
                OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT_FAIL, "open fail"));
                stopSelf();
            }
        } else {
            OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT_FAIL, "open fail: device is null"));
            stopSelf();
        }
    }

    private void closeAccessory() {
        Logger.d("closeAccessory()");
        readThreadGoing = false;

        try {
            Thread.sleep(50);
        } catch (Exception e){
            Logger.e(e, "sleep(50)");
        }

        if(mDevice != null) {
            synchronized(mDevice) {
                if(mDevice.isOpen()) {
                    mDevice.close();
                }
            }
        }

        OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_DISCONNECT));
    }

    private void receiveData(byte[] data) {
        new ReadTask().execute(data);
    }

    private class ReadTask extends AsyncTask<byte[], Void, byte[]> {

        @Override
        protected byte[] doInBackground(byte[]... bytes) {
            return bytes[0];
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_RECEIVE, bytes));
        }

    }

    private final class ReadThread extends Thread {

        @Override
        public void run() {
            while (readThreadGoing) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Logger.e(e, "sleep(50)");
                }
                if (isOpen()) {
                    synchronized (mDevice) {
                        int iavailable = mDevice.getQueueStatus();
                        if (iavailable > 0) {
                            if (iavailable > readSize) {
                                iavailable = readSize;
                            }
                            byte[] data = new byte[iavailable];
                            int num = mDevice.read(data, iavailable);
                            if (num > 0) {
                                receiveData(data);
                            }
                        }
                    }
                }
            }
        }
    }

}
