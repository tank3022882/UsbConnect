package me.qsh.newborn.usbconnect.otto;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * otto 单例封装
 * ============================================================================
 * 版权所有 2018 。
 *
 * @author fallenpanda
 * @version 1.0 2018-03-08 。
 * ============================================================================
 */
public class MainThreadBus extends Bus {

    private static MainThreadBus mBus = new MainThreadBus();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MainThreadBus() {
    }

    public static MainThreadBus getInstance() {
        return mBus;
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }

}
