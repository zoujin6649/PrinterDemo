package com.example.printer.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by claire on 2020/9/3.
 */
public class Bluetooth {
    private BluetoothDevice bluetoothDevice;
    private boolean isConnected;

    public Bluetooth(BluetoothDevice bluetoothDevice, boolean isConnected) {
        this.bluetoothDevice = bluetoothDevice;
        this.isConnected = isConnected;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
