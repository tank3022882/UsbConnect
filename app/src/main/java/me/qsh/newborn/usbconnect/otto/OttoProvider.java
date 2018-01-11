package me.qsh.newborn.usbconnect.otto;

import com.squareup.otto.Bus;

/**
 * otto
 * ============================================================================
 * 版权所有 2018 。
 *
 * @author fallenpanda
 * @version 1.0 2018-01-09 。
 * ============================================================================
 */
public class OttoProvider {

    private static final Bus mBus = new Bus();

    public static Bus getInstance() {
        return mBus;
    }

    private OttoProvider() {
    }

}
