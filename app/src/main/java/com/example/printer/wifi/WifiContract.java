package com.example.printer.wifi;

import com.example.printer.base.IBaseModel;
import com.example.printer.base.IBasePresenter;
import com.example.printer.base.IBaseView;
import com.example.printer.internet.bean.CommonResponse;
import com.example.printer.internet.bean.ListDeviceResponse;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by claire on 2020/9/14.
 */
interface WifiContract {
    interface View extends IBaseView {
        void onGetListDeviceFailure(String errorMsg);

        void refreshListDevices(List<WifiDevice> list);

        void onAddDevice(String errorMsg);

        void onDeleteDeviceSuccess(String deviceID);

        void onDeleteDeviceFailure(String errorMsg);

        void onSendMsgSuccess();

        void onSendMsgFailure(String errorMsg);
    }

    interface Presenter extends IBasePresenter {
        void getListDevices();

        void addDevice(String deviceID, String deviceName);

        void deleteDevice(String deviceID);

        void templatePrint(String deviceID);

        void sendMsg(String deviceID);
    }

    interface Model extends IBaseModel {
        Observable<ListDeviceResponse> getListDevices();

        Observable<CommonResponse> addDevice(String deviceID, String devName);

        Observable<CommonResponse> deleteDevice(String deviceID);

        Observable<CommonResponse> sendMsg(String deviceID);
    }
}
