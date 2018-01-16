package me.qsh.newborn.usbconnect.usb;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import me.qsh.newborn.usbconnect.base.usb.UsbTypeControl;
import me.qsh.newborn.usbconnect.otto.EventData;
import me.qsh.newborn.usbconnect.otto.OttoProvider;
import me.qsh.newborn.usbconnect.utils.ByteUtils;
import me.qsh.newborn.usbconnect.utils.IOUtil;

/**
 * usb Accessory
 * ============================================================================
 * 版权所有 2018 。
 *
 * @author fallenpanda
 * @version 1.0 2018-01-09 。
 * ============================================================================
 */
public class UsbAccessoryService extends Service {

    private final static String ACTION_USB_PERMISSION = "me.qsh.newborn.usbconnect.action.USB_PERMISSION";

    public final static String ACTION_USB_START = "me.qsh.newborn.usbconnect.action.USB_START";
    public final static String ACTION_USB_SEND_DATA = "me.qsh.newborn.usbconnect.action.USB_WRITE";
    public final static String ACTION_EXIT = "me.qsh.newborn.usbconnect.action.EXIT";
    public final static String SEND_DATA = "SEND_DATA";

    // config
    private final int baudRate = 57600; /* baud rate */
    private final byte dataBits = 8; /* 8:8bit, 7: 7bit */
    private final byte stopBits = 1; /* 1:1stop bits, 2:2 stop bits */
    private final byte parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    private final byte flowControl = 0;/* 0:none, 1: flow control(CTS,RTS) */
    private final int readSize = 512;

    private UsbManager mUsbManager;

    private PendingIntent mPendingIntent;

    private ParcelFileDescriptor filedescriptor;
    private FileInputStream mInputstream;
    private FileOutputStream mOutputstream;

    private ReadThread readThread;
    private volatile boolean readThreadGoing = false;

    private final BroadcastReceiver mDetachedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(intent.getAction())) {
                stopSelf();
            }
        }
    };

    private final BroadcastReceiver mPermissionReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_USB_PERMISSION.equals(intent.getAction())) {
                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    openAccessory(accessory);
                }
            }
        }
    };

    public static void startAccessory(Context context, Parcelable parcelable) {
        Intent intent = new Intent(context, UsbAccessoryService.class);
        intent.setAction(ACTION_USB_START);
        intent.putExtra(UsbManager.EXTRA_ACCESSORY, parcelable);
        context.startService(intent);
    }

    public static void exitService(Context context) {
        Intent intent = new Intent(context, UsbAccessoryService.class);
        intent.setAction(ACTION_EXIT);
        context.startService(intent);
    }

    public static void sendData(Context context, byte[] bytes) {
        Intent intent = new Intent(context, UsbAccessoryService.class);
        intent.setAction(ACTION_USB_SEND_DATA);
        intent.putExtra(SEND_DATA, bytes);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("onCreate()");

        this.mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        this.mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        registerReceiver(mDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
        registerReceiver(mPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            Logger.d("onStartCommand() -> "+ action);
            if (ACTION_USB_START.equals(action)) {// 启动
                UsbAccessory accessory = startIntent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory!=null && isMatch(accessory) && isPermitted(accessory)) {
                    openAccessory(accessory);
                }
            } else if (ACTION_USB_SEND_DATA.equals(action)) {// 发送数据帧
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
        unregisterReceiver(mPermissionReceiver);
        super.onDestroy();
    }

    private boolean isMatch(UsbAccessory accessory) {
        // 判断设备匹配

        return true;
    }

    private boolean isPermitted(UsbAccessory accessory) {
        if (mUsbManager.hasPermission(accessory)) {
            return true;
        }
        mUsbManager.requestPermission(accessory, mPendingIntent);
        return false;
    }

    private boolean isOpen() {
        return mInputstream!=null && mOutputstream!=null;
    }

    /**
     * 设置
     */
    private void setConfig() {
        Logger.d("setConfig()");
        byte[] bytes = new byte[8];
        /*prepare the baud rate buffer*/
        bytes[0] = (byte) baudRate;
        bytes[1] = (byte) (baudRate >> 8);
        bytes[2] = (byte) (baudRate >> 16);
        bytes[3] = (byte) (baudRate >> 24);

	    /*data bits*/
        bytes[4] = dataBits;
	    /*stop bits*/
        bytes[5] = stopBits;
	    /*parity*/
        bytes[6] = parity;
        /*flow control*/
        bytes[7] = flowControl;

        writeData(bytes);
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
        try {
            if (isOpen()) {
                int length = data.length;
                if (length > 0) {
                    mOutputstream.write(data, 0, length);
                }
                OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_WRITE_SUCCESS, length));
            } else {
                OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_WRITE_FAIL, "write fail: accessory not open"));
            }
        } catch (IOException e) {
            Logger.e(e, "writeData()");
        }
    }

    private void openAccessory(UsbAccessory accessory) {
        Logger.d("openAccessory()");
        if (accessory != null) {
            filedescriptor = mUsbManager.openAccessory(accessory);
            if (filedescriptor != null) {
                FileDescriptor fd = filedescriptor.getFileDescriptor();
                mInputstream = new FileInputStream(fd);
                mOutputstream = new FileOutputStream(fd);
                if(isOpen()) {
                    setConfig();
                    if (!readThreadGoing) {
                        readThreadGoing = true;

                        readThread = new ReadThread();
                        readThread.start();
                    }
                    OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT, UsbTypeControl.TYPE_USB_ACCESSORY));
                }
            } else {
                OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT_FAIL, "open fail"));
                stopSelf();
            }
        } else {
            OttoProvider.getInstance().post(new EventData(EventData.EVENT_USB_CONNECT_FAIL, "open fail: accessory is null"));
            stopSelf();
        }
    }

    private void closeAccessory() {
        Logger.d("closeAccessory()");
        readThreadGoing = false;

        try {
            Thread.sleep(10);
        } catch (Exception e){
            Logger.e(e, "sleep(10)");
        }

        IOUtil.closeQuietly(filedescriptor);
        IOUtil.closeQuietly(mInputstream);
        IOUtil.closeQuietly(mOutputstream);
        filedescriptor = null;
        mInputstream = null;
        mOutputstream = null;

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
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Logger.e(e, "sleep(50)");
                    }
                    if (isOpen()) {
                        byte[] usbData = new byte[readSize];
                        int available = mInputstream.read(usbData, 0, readSize);
                        if (available > 0) {
                            byte[] data = new byte[available];
                            System.arraycopy(usbData, 0, data, 0, available);
                            receiveData(data);
                        }
                    }
                } catch (IOException e) {
                    Logger.e(e, "read()");
                }
            }
        }
    }

}
