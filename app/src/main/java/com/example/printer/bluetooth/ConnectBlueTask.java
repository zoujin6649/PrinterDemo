package com.example.printer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by claire on 2020/9/3.
 */

/**
 * 蓝牙连接线程
 */
public class ConnectBlueTask extends AsyncTask<BluetoothDevice, Integer, BluetoothSocket> {
    private static final String TAG = "ConnectBlueTask";
    private BluetoothDevice bluetoothDevice;
    private ConnectBlueCallBack callBack;

    public ConnectBlueTask(ConnectBlueCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected BluetoothSocket doInBackground(BluetoothDevice... bluetoothDevices) {
        bluetoothDevice = bluetoothDevices[0];
        BluetoothSocket socket = null;
        try {
//            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(String.valueOf(bluetoothDevice.getUuids()[0])));
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001105-0000-1000-8000-00805f9B34FB"));
//            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
//            socket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
            if (socket != null && !socket.isConnected()) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "socket连接失败");
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.e(TAG, "socket关闭失败");
            }
        }
        return socket;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "开始连接");
        if (callBack != null) callBack.onStartConnect();
    }

    @Override
    protected void onPostExecute(BluetoothSocket bluetoothSocket) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            Log.d(TAG, "连接成功");
            if (callBack != null) callBack.onConnectSuccess(bluetoothDevice, bluetoothSocket);
        } else {
            Log.d(TAG, "连接失败");
            if (callBack != null) callBack.onConnectFail(bluetoothDevice, "连接失败");
        }
    }
}

