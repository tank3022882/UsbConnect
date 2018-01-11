package me.qsh.newborn.usbconnect.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * 基类
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-26 。
 * ============================================================================
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 时间戳
     */
    protected String _timestamp;

    /**
     * Layout 对象
     */
    protected LayoutInflater mInflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set timestamp
        _timestamp = String.valueOf(new Date().getTime());

        setContentView(getLayoutId());

        // bind view
        ButterKnife.bind(this);

        mInflater = getLayoutInflater();

        init(savedInstanceState);
    }

    /**
     * Layout XML
     */
    protected int getLayoutId() {
        return 0;
    }

    /**
     * 初始化View
     */
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 创建View实例
     */
    protected View inflateView(int resId) {
        return mInflater.inflate(resId, null);
    }

}
