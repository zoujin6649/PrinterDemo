package com.example.printer.internet.bean;

import java.util.List;

/**
 * Created by claire on 2020/9/14.
 */
public class ListDeviceResponse {
    /**
     * code : 1
     * msg : 查询成功
     * deviceList : [{"id":132226934,"imsi":"00391282555440580","deviceID":"00391282555440580","title":"GP-SH584wifi","status":"在线"}]
     */

    private int code;
    private String msg;
    private List<DeviceListBean> deviceList;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DeviceListBean> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<DeviceListBean> deviceList) {
        this.deviceList = deviceList;
    }

    public static class DeviceListBean {
        /**
         * id : 132226934
         * imsi : 00391282555440580
         * deviceID : 00391282555440580
         * title : GP-SH584wifi
         * status : 在线
         */

        private int id;
        private String imsi;
        private String deviceID;
        private String title;
        private String status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImsi() {
            return imsi;
        }

        public void setImsi(String imsi) {
            this.imsi = imsi;
        }

        public String getDeviceID() {
            return deviceID;
        }

        public void setDeviceID(String deviceID) {
            this.deviceID = deviceID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
