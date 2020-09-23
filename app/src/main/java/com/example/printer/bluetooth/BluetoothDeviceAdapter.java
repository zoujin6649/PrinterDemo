package com.example.printer.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.printer.R;

import java.util.List;

/**
 * Created by claire on 2020/9/2.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<Bluetooth> list;
    private OnConnClickListener onConnClickListener;
    private onPrintClickListener onPrintClickListener;

    public BluetoothDeviceAdapter(List<Bluetooth> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bluetooth bluetooth = list.get(position);
        BluetoothDevice bluetoothDevice = bluetooth.getBluetoothDevice();
        StringBuilder sb = new StringBuilder();
        sb.append("设备名：");
        sb.append(TextUtils.isEmpty(bluetoothDevice.getName()) ? "未知" : bluetoothDevice.getName());
        sb.append("\nMac地址：");
        sb.append(bluetoothDevice.getAddress());
        holder.tvDeviceName.setText(sb.toString());

        if (bluetooth.isConnected()) {
            holder.btnConnStatus.setText("已连接");
            holder.btnTestPrint.setVisibility(View.VISIBLE);
        } else {
            holder.btnConnStatus.setText("连接");
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
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        Button btnConnStatus;
        Button btnTestPrint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            btnConnStatus = itemView.findViewById(R.id.btn_conn_status);
            btnTestPrint = itemView.findViewById(R.id.btn_test_print);
        }
    }

    public void setOnConnClickListener(OnConnClickListener listener) {
        this.onConnClickListener = listener;
    }

    public void setOnPrintClickListener(onPrintClickListener listener) {
        this.onPrintClickListener = listener;
    }

    public interface OnConnClickListener {
        void onConnClick(int position);
    }

    public interface onPrintClickListener {
        void onPrintClick(int position);
    }
}
