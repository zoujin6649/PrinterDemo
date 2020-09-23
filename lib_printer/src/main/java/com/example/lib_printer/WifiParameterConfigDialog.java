package com.example.lib_printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import static com.example.lib_printer.Constant.MESSAGE_UPDATE_PARAMETER;


/**
 * Created by Administrator
 *
 * @author 猿史森林
 * Date: 2017/11/30
 * Class description:
 */
public class WifiParameterConfigDialog {
    private Activity mContext;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private Handler mHandler;


    public WifiParameterConfigDialog(Activity context, Handler handler) {
        mContext = context;
        this.mHandler = handler;
    }

    public void show() {
        View contentView = View.inflate(mContext, R.layout.dialog_wifi_config, null);
        final EditText etWifiIpConfig = (EditText) contentView.findViewById(R.id.et_wifi_ip);
        final EditText etWifiPortConfig = (EditText) contentView.findViewById(R.id.et_wifi_port);
        sharedPreferencesUtil = SharedPreferencesUtil.getInstantiation(mContext);
        final String ip = sharedPreferencesUtil.getString(Constant.WIFI_DEFAULT_IP, Constant.WIFI_CONFIG_IP);
        final String port = String.valueOf(sharedPreferencesUtil.getInt(Constant.WIFI_DEFAULT_PORT, Constant.WIFI_CONFIG_PORT));
        etWifiIpConfig.setText(ip);
        etWifiPortConfig.setText(port);
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).setView(contentView)
                .setNegativeButton(mContext.getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String strIp = etWifiIpConfig.getText().toString().trim();
                        final String strPort = etWifiPortConfig.getText().toString().trim();
                        System.out.println("strIp --> " + strIp + "\tstrPort --> " + strPort);
                        if (!TextUtils.isEmpty(strIp) && !TextUtils.isEmpty(strPort)) {
                            if (!strIp.equals(ip)) {
                                sharedPreferencesUtil.putString(strIp, Constant.WIFI_CONFIG_IP);
                            }
                            if (!strPort.equals(port)) {
                                sharedPreferencesUtil.putInt(Integer.parseInt(strPort), Constant.WIFI_CONFIG_PORT);
                            }
                            Message message = Message.obtain();
                            message.what = MESSAGE_UPDATE_PARAMETER;
                            Bundle bundle = new Bundle();
                            bundle.putString("Ip", strIp);
                            bundle.putString("Port", strPort);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                    }
                })
                .setPositiveButton(mContext.getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(false);
    }
}
