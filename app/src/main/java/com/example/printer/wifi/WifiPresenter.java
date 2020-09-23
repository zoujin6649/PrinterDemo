package com.example.printer.wifi;

import android.util.Log;

import com.example.printer.base.BasePresenter;
import com.example.printer.internet.bean.CommonResponse;
import com.example.printer.internet.bean.ListDeviceResponse;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by claire on 2020/9/14.
 */
class WifiPresenter extends BasePresenter<WifiContract.View> implements WifiContract.Presenter {

    private WifiModel model;

    public WifiPresenter(WifiContract.View view) {
        super(view);
        model = new WifiModel();
    }

    @Override
    public void getListDevices() {
        model.getListDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ListDeviceResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                        view.showLoadingDialog();
                    }

                    @Override
                    public void onNext(ListDeviceResponse listDeviceResponse) {
                        view.dismissLoadingDialog();
                        if (listDeviceResponse.getCode() == 1) {
                            //查询成功
                            if (listDeviceResponse.getDeviceList() != null && !listDeviceResponse.getDeviceList().isEmpty()) {
                                List<WifiDevice> list = new ArrayList<>();
                                for (ListDeviceResponse.DeviceListBean deviceListBean : listDeviceResponse.getDeviceList()) {
                                    WifiDevice wifiDevice = new WifiDevice();
                                    wifiDevice.setDeviceName(deviceListBean.getTitle());
                                    wifiDevice.setDeviceID(deviceListBean.getDeviceID());
                                    wifiDevice.setStatus(deviceListBean.getStatus());
                                    list.add(wifiDevice);
                                }
                                view.refreshListDevices(list);
                            }
                        } else {
                            view.onGetListDeviceFailure(listDeviceResponse.getCode() + " " + listDeviceResponse.getMsg());
                        }
                        Log.e("TAG", listDeviceResponse.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        view.dismissLoadingDialog();
                        view.onGetListDeviceFailure(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        view.dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void addDevice(String deviceID, String deviceName) {
        model.addDevice(deviceID, deviceName)
                .flatMap(new Function<CommonResponse, ObservableSource<ListDeviceResponse>>() {
                    @Override
                    public ObservableSource<ListDeviceResponse> apply(CommonResponse commonResponse) throws Exception {
                        if (commonResponse.getCode() == 1) {
                            return model.getListDevices();
                        } else {
                            return Observable.error(new Throwable(commonResponse.getCode() + " " + commonResponse.getMsg()));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ListDeviceResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                        view.showLoadingDialog();
                    }

                    @Override
                    public void onNext(ListDeviceResponse listDeviceResponse) {
                        view.dismissLoadingDialog();
                        if (listDeviceResponse.getCode() == 1) {
                            //查询成功
                            if (listDeviceResponse.getDeviceList() != null && !listDeviceResponse.getDeviceList().isEmpty()) {
                                List<WifiDevice> list = new ArrayList<>();
                                for (ListDeviceResponse.DeviceListBean deviceListBean : listDeviceResponse.getDeviceList()) {
                                    WifiDevice wifiDevice = new WifiDevice();
                                    wifiDevice.setDeviceName(deviceListBean.getTitle());
                                    wifiDevice.setDeviceID(deviceListBean.getDeviceID());
                                    wifiDevice.setStatus(deviceListBean.getStatus());
                                    list.add(wifiDevice);
                                }
                                view.refreshListDevices(list);
                            }
                        } else {
                            view.onAddDevice(listDeviceResponse.getCode() + " " + listDeviceResponse.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.dismissLoadingDialog();
                        view.onAddDevice(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        view.dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void deleteDevice(String deviceID) {
        model.deleteDevice(deviceID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommonResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(CommonResponse commonResponse) {
                        view.dismissLoadingDialog();
                        if (commonResponse.getCode() == 1) {
                            view.onDeleteDeviceSuccess(deviceID);
                        } else {
                            view.onDeleteDeviceFailure(commonResponse.getCode() + " " + commonResponse.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.dismissLoadingDialog();
                        view.onDeleteDeviceFailure(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        view.dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void templatePrint(String deviceID) {
    }

    @Override
    public void sendMsg(String deviceID) {
        model.sendMsg(deviceID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommonResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(CommonResponse commonResponse) {
                        view.showLoadingDialog();
                        if (commonResponse.getCode() == 0) {
                            view.onSendMsgSuccess();
                        } else {
                            view.onSendMsgFailure(commonResponse.getCode() + " " + commonResponse.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.dismissLoadingDialog();
                        view.onSendMsgFailure(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        view.dismissLoadingDialog();
                    }
                });
    }

}
