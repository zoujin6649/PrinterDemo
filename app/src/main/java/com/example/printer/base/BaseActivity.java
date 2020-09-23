package com.example.printer.base;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.printer.CursorDialog;
import com.example.printer.utils.DialogUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by Claire on 2020/5/7
 */
public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity {

    protected T presenter;
    private Queue<WeakReference<CursorDialog>> mLoadingDialogQueue = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        presenter = getPresenter();
        initView(savedInstanceState);
        processLogic();
    }

    protected void showNormalLoadingDialog() {
        CursorDialog loadingDialog = DialogUtils.getProgressWheelDialog(this);
        mLoadingDialogQueue.offer(new WeakReference<>(loadingDialog));
        if (!isFinishing()) {
            loadingDialog.show();
        }
    }

    protected void dismissNormalLoadingDialog() {
        if (mLoadingDialogQueue == null) {
            return;
        }
        WeakReference<CursorDialog> weak = mLoadingDialogQueue.poll();
        if (weak == null) {
            return;
        }
        CursorDialog loadingDialog = weak.get();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.dismiss();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
            presenter = null;
        }
        dismissNormalLoadingDialog();
        super.onDestroy();
    }

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void processLogic();

    protected abstract int getResLayout();

    protected abstract T getPresenter();


    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //获得当前得到焦点的view，一般情况下就是EditText（特殊情况就是轨迹求或者实体按键会移动焦点）
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    protected boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationOnScreen(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    protected void hideSoftInput(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im == null) {
            return;
        }
        im.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}

