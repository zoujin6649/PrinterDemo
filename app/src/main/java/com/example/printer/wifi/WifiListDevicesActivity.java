package com.example.printer.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.printer.CursorDialog;
import com.example.printer.R;
import com.example.printer.base.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by claire on 2020/9/14.
 */
public class WifiListDevicesActivity extends BaseActivity<WifiPresenter> implements WifiContract.View {

    Button btnAdd;
    RecyclerView recyclerView;
    private WifiDeviceAdapter adapter;
    private List<WifiDevice> list = new ArrayList<>();

    private WeakReference<Activity> weakActivity;
    private Dialog addDialog;

    @Override
    protected void initView(Bundle savedInstanceState) {
        weakActivity = new WeakReference<Activity>(this);
        btnAdd = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new WifiDeviceAdapter(list);
        adapter.setOnDeleteClickListener(new WifiDeviceAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                presenter.deleteDevice(list.get(position).getDeviceID());
            }
        });
        adapter.setOnPrintClickListener(new WifiDeviceAdapter.OnPrintClickListener() {
            @Override
            public void onPrintClick(int position) {
                presenter.sendMsg(list.get(position).getDeviceID());
            }
        });
        adapter.setOnSetDefaultClickListener(new WifiDeviceAdapter.OnSetDefaultClickListener() {
            @Override
            public void onSetDefaultClick(int position) {

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        if (weakActivity == null) {
            return;
        }
        if (weakActivity.get() == null) {
            return;
        }
        if (addDialog != null && addDialog.isShowing()) {
            addDialog.dismiss();
        }
        addDialog = new CursorDialog.Builder(weakActivity.get())
                .notFloating()
                .full()
                .setlayout(R.layout.dialog_add)
                .setViewClick(R.id.btn_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addDialog.dismiss();
                    }
                })
                .build();
        EditText etDeviceNumber = addDialog.findViewById(R.id.et_device_number);
        EditText etDeviceName = addDialog.findViewById(R.id.et_device_name);
        Button btnSave = addDialog.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog.dismiss();
                presenter.addDevice(etDeviceNumber.getText().toString().trim(), etDeviceName.getText().toString().trim());
            }
        });
        if (!weakActivity.get().isFinishing()) {
            addDialog.show();
        }
    }

    @Override
    protected void processLogic() {
        presenter.getListDevices();
    }

    @Override
    protected int getResLayout() {
        return R.layout.activity_wifi_device;
    }

    @Override
    protected WifiPresenter getPresenter() {
        return new WifiPresenter(this);
    }

    @Override
    public void showLoadingDialog() {
        showNormalLoadingDialog();
    }

    @Override
    public void dismissLoadingDialog() {
        dismissNormalLoadingDialog();
    }

    @Override
    public void showNetworkErrorDialog() {

    }

    @Override
    public void onGetListDeviceFailure(String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void onAddDevice(String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void onDeleteDeviceSuccess(String deviceID) {
        Iterator<WifiDevice> iterator = list.iterator();
        while (iterator.hasNext()) {
            WifiDevice next = iterator.next();
            if (next.getDeviceID().equalsIgnoreCase(deviceID)) {
                iterator.remove();
                adapter.notifyDataSetChanged();
                break;
            }
        }

    }

    @Override
    public void onDeleteDeviceFailure(String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void onSendMsgSuccess() {
        showToast("发送成功");
    }

    @Override
    public void onSendMsgFailure(String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void refreshListDevices(List<WifiDevice> devices) {
        if (devices != null && !devices.isEmpty()) {
            list.clear();
            list.addAll(devices);
            adapter.notifyDataSetChanged();
        }
    }
}
