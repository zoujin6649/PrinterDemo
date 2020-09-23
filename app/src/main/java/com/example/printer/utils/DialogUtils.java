package com.example.printer.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.example.printer.CursorDialog;


/**
 * Created by Claire on 2020/5/9
 */

public class DialogUtils {
    private Dialog networkErrorDialog;
    private Dialog resendDialog;
    private Dialog logoutDialog;
    private Dialog forceLogoutDialog;
    private CursorDialog registerDialog;
    private Dialog mNoSupportDialog;

    public static CursorDialog getProgressWheelDialog(Context context) {
        RelativeLayout layout = new RelativeLayout(context);

        ProgressWheel view = new ProgressWheel(context);
        view.setBarColor(Color.rgb(0x00, 0x00, 0x00));
        view.spin();//旋转
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);//定位布局

        view.setLayoutParams(params);
        layout.addView(view);

        CursorDialog dialog = new CursorDialog.Builder(context)
                .notFloating()
                .full()
                .setView(layout)
                .build();
        dialog.setCancelable(false);
        return dialog;
    }

    public static ProgressDialog getProgressDialog(Context context, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

}

