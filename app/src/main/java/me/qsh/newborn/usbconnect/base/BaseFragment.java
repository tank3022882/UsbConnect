package me.qsh.newborn.usbconnect.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * 基类
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-25 。
 * ============================================================================
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    /**
     * 时间戳
     */
    protected String _timestamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set timestamp
        _timestamp = String.valueOf(new Date().getTime());

        init(savedInstanceState);
    }

    /**
     * 初始化
     */
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        // bind view
        ButterKnife.bind(this, view);
        initView(view);
        return view;
    }

    /**
     * layout xml
     */
    protected int getLayoutId() {
        return 0;
    }

    /**
     * 初始化 View
     */
    protected void initView(View view) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View view) {

    }

}
