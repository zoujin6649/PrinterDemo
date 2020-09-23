package com.example.printer.wifi;

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
public class WifiDeviceAdapter extends RecyclerView.Adapter<WifiDeviceAdapter.ViewHolder> {

    private List<WifiDevice> list;
    private OnPrintClickListener onPrintClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnSetDefaultClickListener onSetDefaultClickListener;

    public WifiDeviceAdapter(List<WifiDevice> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_device, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiDevice device = list.get(position);
        holder.tvDeviceName.setText(device.getDeviceName());
        holder.tvDeviceNumber.setText(device.getDeviceID());
        holder.tvDeviceStatus.setText(device.getStatus());
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(position);
                }
            }
        });
        holder.btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onPrintClickListener != null) {
                    onPrintClickListener.onPrintClick(position);
                }
            }
        });
        holder.btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSetDefaultClickListener != null) {
                    onSetDefaultClickListener.onSetDefaultClick(position);
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
        TextView tvDeviceNumber;
        TextView tvDeviceStatus;
        Button btnDelete;
        Button btnDefault;
        Button btnPrint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceStatus = itemView.findViewById(R.id.tv_status);
            tvDeviceNumber = itemView.findViewById(R.id.tv_device_Number);
            btnDefault = itemView.findViewById(R.id.btn_default);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnPrint = itemView.findViewById(R.id.btn_test_print);
        }
    }


    public void setOnPrintClickListener(OnPrintClickListener onPrintClickListener) {
        this.onPrintClickListener = onPrintClickListener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setOnSetDefaultClickListener(OnSetDefaultClickListener onSetDefaultClickListener) {
        this.onSetDefaultClickListener = onSetDefaultClickListener;
    }

    public interface OnPrintClickListener {
        void onPrintClick(int position);
    }

    public interface OnSetDefaultClickListener {
        void onSetDefaultClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }
}
