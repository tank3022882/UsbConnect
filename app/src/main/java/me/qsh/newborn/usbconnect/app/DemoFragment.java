package me.qsh.newborn.usbconnect.app;

import android.annotation.SuppressLint;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import me.qsh.newborn.usbconnect.R;
import me.qsh.newborn.usbconnect.base.usb.BaseUsbFragment;
import me.qsh.newborn.usbconnect.utils.ByteUtils;

/**
 * 测试
 * ============================================================================
 * 版权所有 2017 。
 *
 * @author fallenpanda
 * @version 1.0 2017-12-25 。
 * ============================================================================
 */
@SuppressLint("ValidFragment")
public class DemoFragment extends BaseUsbFragment {

    @Bind(R.id.tv_text)
    TextView mTvText;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_demo;
    }

    @Override
    protected void initView(View view) {

        mTvText.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @OnClick(R.id.btn_write_1)
    void btn_write1_click(View view) {
        // 数据
        byte[] bytes = {1, 2, 3, 4, 5, 6};

        StringBuffer str = new StringBuffer("*********** write start ***********\n");
        for (byte b: bytes) {
            str.append(ByteUtils.byteToInt(b)+" ");
        }
        str.append("\n*********** write end ***********\n");
        mTvText.append(str);

        sendData(bytes);
    }

    @OnClick(R.id.btn_clear)
    public void btn_clear_click(View view) {
        mTvText.setText("");
    }

    @Override
    public void notifyUsbWriteSuccess(int num) {
        mTvText.append("notifyUsbWriteSuccess() -> "+num+"\n");
    }

    @Override
    public void notifyUsbWriteFailed(String errorMsg) {
        mTvText.append("notifyUsbWriteFailed() -> "+errorMsg+"\n");
    }

    @Override
    public void notifyUsbDataReceive(byte[] bytes) {
        StringBuffer str = new StringBuffer("*********** receive start ***********\n");
        for (byte b: bytes) {
            str.append(ByteUtils.byteToInt(b)+" ");
        }
        str.append("\n*********** receive end ***********\n");
        mTvText.append(str);
    }

}
