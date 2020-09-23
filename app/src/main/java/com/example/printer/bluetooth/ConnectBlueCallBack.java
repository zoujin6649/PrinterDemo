package com.example.printer.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Created by claire on 2020/9/3.
 */
interface ConnectBlueCallBack {
    void onStartConnect();

    void onConnectSuccess(BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket);

    void onConnectFail(BluetoothDevice bluetoothDevice, String 连接失败);
}
