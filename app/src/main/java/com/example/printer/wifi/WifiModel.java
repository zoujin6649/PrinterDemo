package com.example.printer.wifi;

import com.example.lib_printer.PrintUtils;
import com.example.printer.internet.IWifi;
import com.example.printer.internet.RetrofitUtils;
import com.example.printer.internet.ServerInterface;
import com.example.printer.internet.bean.CommonResponse;
import com.example.printer.internet.bean.ListDeviceResponse;
import com.example.printer.utils.Md5Utils;

import io.reactivex.Observable;
import retrofit2.Retrofit;

/**
 * Created by claire on 2020/9/14.
 */
class WifiModel implements WifiContract.Model {
    @Override
    public Observable<ListDeviceResponse> getListDevices() {
        Retrofit retrofit = RetrofitUtils.getGsonRetrofit();
        String url = ServerInterface.GET_LIST_DEVICE;
//        String memberCode = "ac20936d154c4d48af7504eca626405c";
        String memberCode = "2ba04cee2a7842b7abdcdfbf02066a24";
        String reqTime = String.valueOf(System.currentTimeMillis());
//        String apiKey = "KXHR81ES";
        String apiKey = "9J7ZN4VP";
        String securityCode = Md5Utils.md5(memberCode + reqTime + apiKey);
        return retrofit.create(IWifi.class).getListDevices(url, reqTime, memberCode, securityCode);
    }

    @Override
    public Observable<CommonResponse> addDevice(String deviceID, String devName) {
        Retrofit retrofit = RetrofitUtils.getGsonRetrofit();
        String url = ServerInterface.ADD_DEVICE;
        String memberCode = "2ba04cee2a7842b7abdcdfbf02066a24";
        String reqTime = String.valueOf(System.currentTimeMillis());
        String apiKey = "9J7ZN4VP";
        String securityCode = Md5Utils.md5(memberCode + reqTime + apiKey + deviceID);
        return retrofit.create(IWifi.class).addDevice(url, reqTime, securityCode, memberCode, deviceID, devName);
    }

    @Override
    public Observable<CommonResponse> deleteDevice(String deviceID) {
        Retrofit retrofit = RetrofitUtils.getGsonRetrofit();
        String url = ServerInterface.DELETE_DEVICE;
        String memberCode = "2ba04cee2a7842b7abdcdfbf02066a24";
        String reqTime = String.valueOf(System.currentTimeMillis());
        String apiKey = "9J7ZN4VP";
        String securityCode = Md5Utils.md5(memberCode + reqTime + apiKey + deviceID);
        return retrofit.create(IWifi.class).deleteDevice(url, reqTime, securityCode, memberCode, deviceID);
    }

    @Override
    public Observable<CommonResponse> sendMsg(String deviceID) {
        Retrofit retrofit = RetrofitUtils.getGsonRetrofit();
        String url = ServerInterface.SEND_MSG;
        String memberCode = "2ba04cee2a7842b7abdcdfbf02066a24";
        String reqTime = String.valueOf(System.currentTimeMillis());
        String apiKey = "9J7ZN4VP";
        String securityCode = Md5Utils.md5(memberCode + deviceID + reqTime + apiKey);
        String mode = "2";
        String msgDetail = "<gpLogo/><gpWord Align=1 Bold=1 Wsize=2 Hsize=2 Reverse=0 Underline=0>发货单</gpWord>\n" +
                "<gpBarCode Align=1 Type=7 Width=2 Height=80 Position=0>201811080001</gpBarCode>\n" +
                "<gpWord Align=0 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>订单编号：201811080001</gpWord>\n" +
                "<gpWord Align=0 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>买家姓名：张三</gpWord>\n" +
                "<gpWord Align=0 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>买家手机：18666666666</gpWord>\n" +
                "<gpWord Align=0 Bold=1 Wsize=0 Hsize=1 Reverse=0 Underline=0>买家留言：发顺丰,尽快发货，谢谢</gpWord>\n" +
                "<gpWord Align=0 Bold=1 Wsize=0 Hsize=1 Reverse=0 Underline=0>卖家备注：发顺丰，优先处理</gpWord>\n" +
                "<gpWord Align=0 Bold=1 Wsize=0 Hsize=1 Reverse=0 Underline=0>买就送信息：送U盘</gpWord>\n" +
                "<gpWord Align=0 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>--------------------------------</gpWord>\n" +
                "<gpTR4 Type=0><td>宝贝名称</td><td>单价</td><td>数量</td><td>价格</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>佳博GP-CH421D云打印机</td><td>1180</td><td>1</td><td>1180</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>佳博GP-5890XIII云打印机</td><td>480</td><td>1</td><td>480</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>佳博G3-350V云打印机</td><td>980</td><td>1</td><td>980</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>100x150热敏标签纸300</td><td>36</td><td>10</td><td>360</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>58毫米热敏卷纸100米</td><td>48</td><td>5</td><td>240</td></gpTR4>\n" +
                "<gpTR4 Type=0><td>80毫米热敏卷纸100米</td><td>42</td><td>10</td><td>420</td></gpTR4>\n" +
                "<gpWord Align=0 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>-------------------------------- </gpWord>\n" +
                "<gpWord Align=2 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>合计：3660元</gpWord>\n" +
                "<gpWord Align=2 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>优惠：-198元</gpWord>\n" +
                "<gpWord Align=2 Bold=0 Wsize=0 Hsize=0 Reverse=0 Underline=0>邮费：  30元</gpWord>\n" +
                "<gpWord Align=2 Bold=1 Wsize=1 Hsize=1 Reverse=0 Underline=0>实收：3492元 </gpWord>\n" +
                "<gpCut/>\n" +
                "<gpWord Align=1 Bold=1 Wsize=1 Hsize=1 Reverse=0 Underline=0>扫码关注佳博</gpWord>\n" +
                "<gpQRCode Align=1 Size=9 Error=M>http://weixin.qq.com/r/kHV3b67EXPMjreoM9yCC</gpQRCode>\n" +
                "<gpCut/>";
        return retrofit.create(IWifi.class).sendMsg(url, reqTime, securityCode, memberCode, deviceID, mode, msgDetail);
    }
}
