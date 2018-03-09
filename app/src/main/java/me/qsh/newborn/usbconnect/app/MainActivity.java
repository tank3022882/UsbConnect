package me.qsh.newborn.usbconnect.app;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import me.qsh.newborn.usbconnect.R;
import me.qsh.newborn.usbconnect.base.BaseActivity;
import me.qsh.newborn.usbconnect.base.usb.UsbControl;
import me.qsh.newborn.usbconnect.base.usb.UsbTypeControl;
import me.qsh.newborn.usbconnect.otto.EventData;
import me.qsh.newborn.usbconnect.otto.MainThreadBus;
import me.qsh.newborn.usbconnect.usb.UsbAccessoryService;
import me.qsh.newborn.usbconnect.usb.UsbFTDeviceService;

/**
 * 首页
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-25 。
 * ============================================================================
 */
public class MainActivity extends BaseActivity implements UsbTypeControl, NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    private int mCurrentMenuId;

    private UsbManager mUsbManager;

    private boolean isConnect = false;
    private int mType = 0x00;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        this.mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        MainThreadBus.getInstance().register(this);
        initIntent(getIntent());

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        // 第一个界面
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new DemoFragment();
        fragmentTransaction.replace(R.id.content, fragment, "F_" + mCurrentMenuId);
        fragmentTransaction.commit();

        mNavigationView.setCheckedItem(R.id.nav_demo);
    }

    private void initIntent(Intent intent) {
        if (intent != null) {
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                UsbFTDeviceService.startDevice(this);
            } else if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
                UsbAccessoryService.startAccessory(this, intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY));
            }
        }
    }

    @Subscribe
    public void busEvent(EventData data) {
        switch (data.eventType) {
            case EventData.EVENT_USB_CONNECT:
                isConnect = true;
                mType = (Integer) data.eventData;
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbConnect(mType);
                }
                break;
            case EventData.EVENT_USB_CONNECT_FAIL:
                isConnect = false;
                fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbConnectFailed((String) data.eventData);
                }
                break;
            case EventData.EVENT_USB_DISCONNECT:
                isConnect = false;
                fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbDisconnect();
                }
                break;
            case EventData.EVENT_USB_WRITE_SUCCESS:
                fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbWriteSuccess((Integer) data.eventData);
                }
                break;
            case EventData.EVENT_USB_WRITE_FAIL:
                fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbWriteFailed((String) data.eventData);
                }
                break;
            case EventData.EVENT_USB_RECEIVE:
                fragment = getCurrentFragment();
                if (fragment instanceof UsbControl) {
                    ((UsbControl) fragment).notifyUsbDataReceive((byte[]) data.eventData);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        UsbFTDeviceService.exitService(this);
        UsbAccessoryService.exitService(this);
        MainThreadBus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        initIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mCurrentMenuId = item.getItemId();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;

        if (mCurrentMenuId == R.id.nav_demo) {
            fragment = new DemoFragment();
        }

        if (fragment != null) {
            fragmentTransaction.replace(R.id.content, fragment, "F_" + mCurrentMenuId);
            fragmentTransaction.commit();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag("F_" + mCurrentMenuId);
    }

    @Override
    public int connectType() {
        if (isConnect) {
            return mType;
        }
        return UsbTypeControl.TYPE_USB_NONE;
    }

}
