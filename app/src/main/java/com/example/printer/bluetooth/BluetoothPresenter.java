package com.example.printer.bluetooth;

import com.example.printer.base.BasePresenter;

/**
 * Created by claire on 2020/9/3.
 */
class BluetoothPresenter extends BasePresenter<BluetoothContract.View> implements BluetoothContract.Presenter {

    private BluetoothModel model;

    public BluetoothPresenter(BluetoothContract.View view) {
        super(view);
        model = new BluetoothModel();
    }
}
