package com.example.printer;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lib_printer.DeviceConnFactoryManager;
import com.example.lib_printer.PrinterSDK;
import com.example.printer.bluetooth.BluetoothDeviceAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by claire on 2020/9/15.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    public static final String TITLE = "title";
    public static final String STATUS = "status";
    public static final String BT_ENABLE = "btenable";
    public static final String ENABLE = "enable";
    public static final String DISABLE = "disable";
    public static final int MESSAGE_CONNECT = 1;
    public static final String CONN_METHOD = "conn method";
    public static final String MAC_ADDRESS = "mac_address";
    public static final String IP = "ip";
    public static final String PORT = "port";
    private List<Map<String, Object>> listItems;

    private BluetoothDeviceAdapter.OnConnClickListener onConnClickListener;
    private BluetoothDeviceAdapter.onPrintClickListener onPrintClickListener;

    public DeviceAdapter(List<Map<String, Object>> listItems) {
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        DeviceAdapter.ViewHolder holder = new DeviceAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        String deviceName = (String) listItems.get(position).get(TITLE);
        String connMethod = (String) listItems.get(position).get(CONN_METHOD);
        String status = (String) listItems.get(position).get(STATUS);
        if (DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH.toString().equals(connMethod)) {
            //蓝牙
            String macAddress = (String) listItems.get(position).get(MAC_ADDRESS);
            String info = PrinterSDK.getApplication().getString(R.string.str_address) + (TextUtils.isEmpty(macAddress) ? "" : macAddress);
            holder.tvDeviceInfo.setText(info.trim());
            String str = (String) listItems.get(position).get(BT_ENABLE);
            if (ENABLE.equals(str)) {
                holder.btnConnStatus.setEnabled(true);
                holder.btnTestPrint.setEnabled(true);
            } else {
                holder.btnConnStatus.setEnabled(false);
                holder.btnTestPrint.setEnabled(false);
            }
        } else if (DeviceConnFactoryManager.CONN_METHOD.WIFI.toString().equals(connMethod)) {
            //WIFI
            String ip = (String) listItems.get(position).get(IP);
            String port = (String) listItems.get(position).get(PORT);
            String info = PrinterSDK.getApplication().getString(R.string.str_ip) + "  " + PrinterSDK.getApplication().getString(R.string.str_port);
            holder.tvDeviceInfo.setText(info.trim());
        }
        holder.tvDeviceName.setText(TextUtils.isEmpty(deviceName) ? "未知" : deviceName);
        holder.btnConnStatus.setText(status);
        if (PrinterSDK.getApplication().getString(R.string.str_disconn).equals(status)) {
            holder.btnTestPrint.setVisibility(View.VISIBLE);
        } else {
            holder.btnTestPrint.setVisibility(View.GONE);
        }
        holder.btnConnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onConnClickListener != null) {
                    onConnClickListener.onConnClick(position);
                }
            }
        });
        holder.btnTestPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPrintClickListener != null) {
                    onPrintClickListener.onPrintClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems == null ? 0 : listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        TextView tvDeviceInfo;
        Button btnConnStatus;
        Button btnTestPrint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceInfo = itemView.findViewById(R.id.tv_device_info);
            btnConnStatus = itemView.findViewById(R.id.btn_conn_status);
            btnTestPrint = itemView.findViewById(R.id.btn_test_print);
        }
    }

    public void setOnConnClickListener(BluetoothDeviceAdapter.OnConnClickListener listener) {
        this.onConnClickListener = listener;
    }

    public void setOnPrintClickListener(BluetoothDeviceAdapter.onPrintClickListener listener) {
        this.onPrintClickListener = listener;
    }

    public interface OnConnClickListener {
        void onConnClick(int position);
    }

    public interface onPrintClickListener {
        void onPrintClick(int position);
    }
}
