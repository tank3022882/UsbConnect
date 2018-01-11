package me.qsh.newborn.usbconnect.base;

import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import me.qsh.newborn.usbconnect.ui.toast.SimplexToast;

public abstract class BaseApplication extends Application {

	protected static Context _context;

	@Override
	public void onCreate() {
		super.onCreate();
		_context = getApplicationContext();
		init();
	}

	protected void init() {
	}

	public static synchronized BaseApplication context() {
		return (BaseApplication) _context;
	}

	public static void showToast(int message) {
		showToast(message, Toast.LENGTH_LONG, 0);
	}

	public static void showToast(String message) {
		showToast(message, Toast.LENGTH_LONG, 0, Gravity.BOTTOM);
	}

	public static void showToast(int message, int icon) {
		showToast(message, Toast.LENGTH_LONG, icon);
	}

	public static void showToast(String message, int icon) {
		showToast(message, Toast.LENGTH_LONG, icon, Gravity.BOTTOM);
	}

	public static void showToastShort(int message) {
		showToast(message, Toast.LENGTH_SHORT, 0);
	}

	public static void showToastShort(String message) {
		showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM);
	}

	public static void showToastShort(int message, Object... args) {
		showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM, args);
	}

	public static void showToast(int message, int duration, int icon) {
		showToast(message, duration, icon, Gravity.BOTTOM);
	}

	public static void showToast(int message, int duration, int icon,
								 int gravity) {
		showToast(context().getString(message), duration, icon, gravity);
	}

	public static void showToast(int message, int duration, int icon,
								 int gravity, Object... args) {
		showToast(context().getString(message, args), duration, icon, gravity);
	}

	public static void showToast(String message, int duration, int icon, int gravity) {
		Context context = _context;
		if (context != null)
			SimplexToast.show(context, message, gravity, duration);
	}

}
