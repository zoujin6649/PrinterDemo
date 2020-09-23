package com.example.lib_printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.lib_printer.Constant.ACTION_USB_PERMISSION;
import static com.example.lib_printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.example.lib_printer.DeviceConnFactoryManager.CONN_STATE_FAILED;
import static com.example.lib_printer.ListViewAdapter.CONN_METHOD;
import static com.example.lib_printer.ListViewAdapter.MESSAGE_CONNECT;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 * Date: 2017/11/3
 * Class description:该类用于连接多台打印机
 */
public class ConnMoreDevicesActivity extends Activity {
    String TAG = ConnMoreDevicesActivity.class.getSimpleName();
    private Map<String, DeviceConnFactoryManager> deviceConnFactoryManagers;
    private int id = 0;
    private ListView listView;
    private ListViewAdapter adapter;
    private List<Map<String, Object>> lists;
    private byte[] sendCommand;
    private ThreadFactoryBuilder threadFactoryBuilder;
    private String usbName;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;
    private ThreadPool threadPool = null;
    /**
     * 广播监听
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                usbConn(device);
                            }
                        } else {//用户拒绝访问USB权限
                            Utils.toast(ConnMoreDevicesActivity.this, "请给予访问USB权限");
                        }
                    }
                    break;
                //Usb连接断开
                case ACTION_USB_DEVICE_DETACHED:
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    for (int i = 0; i < 4; i++) {
                        if (deviceConnFactoryManagers.get(i) != null && deviceConnFactoryManagers.get(i).usbDevice().equals(usbDevice)) {
                            deviceConnFactoryManagers.get(i).closePort();
                            lists.clear();
                            lists = getOperateItemData();
                            adapter.notifyDataSetChanged(lists);
                            Utils.toast(ConnMoreDevicesActivity.this, getString(R.string.disconnect) + "\t" + i);
                            return;
                        }
                    }
                    break;
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            Log.e(TAG, "lost id:" + id);
                            id = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                            Utils.toast(ConnMoreDevicesActivity.this, getString(R.string.str_conn_state_disconnect));
                            lists.get(id).put(ListViewAdapter.STATUS, getString(R.string.connect));
                            adapter.notifyDataSetChanged();
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            Utils.toast(ConnMoreDevicesActivity.this, getString(R.string.connecting));
                            lists.get(id).put(ListViewAdapter.STATUS, getString(R.string.str_connecting));
                            adapter.notifyDataSetChanged();
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            Utils.toast(ConnMoreDevicesActivity.this, getString(R.string.str_conn_state_connected));
                            lists.get(id).put(ListViewAdapter.STATUS, getString(R.string.str_disconn));
                            adapter.notifyDataSetChanged();
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(ConnMoreDevicesActivity.this, getString(R.string.str_conn_fail));
                            lists.get(id).put(ListViewAdapter.STATUS, getString(R.string.connect));
                            adapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connmoredivices);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        threadPool = ThreadPool.getInstantiation();
        threadFactoryBuilder = new ThreadFactoryBuilder("ConnMoreDevicesActivity");
        deviceConnFactoryManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
        listView = (ListView) findViewById(R.id.lv_printer_lists);
        lists = getOperateItemData();
        adapter = new ListViewAdapter(this, lists, mHandler);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                id = i;
                if (deviceConnFactoryManagers.get(id) == null || !deviceConnFactoryManagers.get(id).getConnState()) {
                    showDialog();
                } else {
                    returnResult();
                }
            }
        });
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, filter);
    }

    private void returnResult() {
        Utils.toast(ConnMoreDevicesActivity.this, MessageFormat.format(getString(R.string.str_select_printer_id), id));
        Intent intent = new Intent();
        intent.putExtra("id", id);
        setResult(RESULT_OK, intent);
        finish();
    }

    private List<Map<String, Object>> getOperateItemData() {
        int[] PrinterID = new int[]{R.string.gprinter001, R.string.gprinter002, R.string.gprinter003,
                R.string.gprinter004};
        int[] PrinterImage = new int[]{R.drawable.ic_printer, R.drawable.ic_printer, R.drawable.ic_printer,
                R.drawable.ic_printer};
        Map<String, Object> map;
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < PrinterID.length; i++) {
            map = new HashMap<>();
            map.put(ListViewAdapter.IMG, PrinterImage[i]);
            map.put(ListViewAdapter.TITEL, getString(PrinterID[i]));
            if (deviceConnFactoryManagers.get(i) != null && deviceConnFactoryManagers.get(i).getConnMethod() != null) {
                if (deviceConnFactoryManagers.get(i).getConnState()) {
                    map.put(ListViewAdapter.STATUS, getString(R.string.str_disconn));
                } else {
                    map.put(ListViewAdapter.STATUS, getString(R.string.connect));
                }
                String connMethodName = deviceConnFactoryManagers.get(i).getConnMethod().toString();
                String info = getPortParamInfoString(deviceConnFactoryManagers.get(i));
                map.put(CONN_METHOD, connMethodName);
                map.put(ListViewAdapter.INFO, info);
                map.put(ListViewAdapter.BT_ENABLE, "enable");
                list.add(map);
            } else {
                String info = getPortParamInfoString(null);
                map.put(CONN_METHOD, getString(R.string.str_interface_undefine));
                map.put(ListViewAdapter.INFO, info);
                map.put(ListViewAdapter.BT_ENABLE, "enable");
                map.put(ListViewAdapter.STATUS, getString(R.string.connect));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * @param deviceConnFactoryManager
     * @return
     */
    private String getPortParamInfoString(DeviceConnFactoryManager deviceConnFactoryManager) {
        String info = new String();
        if (deviceConnFactoryManager != null) {
            DeviceConnFactoryManager.CONN_METHOD type = deviceConnFactoryManager.getConnMethod();
            if (type == null) {
                info = getString(R.string.port);
            }
            if (type == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) {
                info += getString(R.string.str_bluetooth);
                info += "  " + getString(R.string.str_address);
                info += deviceConnFactoryManager.getMacAddress();
            } else if (type == DeviceConnFactoryManager.CONN_METHOD.USB) {
                info += getString(R.string.str_usb);
                info += "  " + getString(R.string.str_address);
                UsbDevice usbDevice = deviceConnFactoryManager.usbDevice();
                if (usbDevice != null) {
                    info += usbDevice.getDeviceName();
                }
                info += " ";
            } else if (type == DeviceConnFactoryManager.CONN_METHOD.WIFI) {
                info += getString(R.string.str_wifi);
                info += "  " + getString(R.string.str_ip);
                info += deviceConnFactoryManager.getIp();
                info += "  " + getString(R.string.str_port);
                info += deviceConnFactoryManager.getPort();
            } else if (type == DeviceConnFactoryManager.CONN_METHOD.SERIAL_PORT) {

            }
        } else {
            info = getString(R.string.init_port_info);
        }
        return info;
    }

    private void showDialog() {
        new AlertDialog.Builder(this)
                .setItems(getResources().getStringArray(R.array.str_interf_type), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                startActivityForResult(new Intent(ConnMoreDevicesActivity.this, BluetoothDeviceList.class), Constant.BLUETOOTH_REQUEST_CODE);
                                break;
                            case 1:
                                WifiParameterConfigDialog wifiParameterConfigDialog = new WifiParameterConfigDialog(ConnMoreDevicesActivity.this, mHandler);
                                wifiParameterConfigDialog.show();
                                break;
                            case 2:
                                startActivityForResult(new Intent(ConnMoreDevicesActivity.this, UsbDeviceList.class), Constant.USB_REQUEST_CODE);
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                /*蓝牙连接*/
                case Constant.BLUETOOTH_REQUEST_CODE: {
                    /*获取蓝牙mac地址*/
                    String macAddress = data.getStringExtra(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
                    //初始化话DeviceConnFactoryManager
                    new DeviceConnFactoryManager.Build()
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .setId(macAddress)
                            .build();
                    lists.get(id).put(CONN_METHOD, "蓝牙");
                    lists.get(id).put(ListViewAdapter.INFO, getPortParamInfoString(deviceConnFactoryManagers.get(id)));
                    adapter.notifyDataSetChanged();
                    break;
                }
                /*USB连接*/
                case Constant.USB_REQUEST_CODE: {
                    //获取USB设备名
                    usbName = data.getStringExtra(UsbDeviceList.USB_NAME);
                    //通过USB设备名找到USB设备
                    UsbDevice usbDevice = Utils.getUsbDeviceFromName(ConnMoreDevicesActivity.this, usbName);
                    //判断USB设备是否有权限
                    if (usbManager.hasPermission(usbDevice)) {
                        usbConn(usbDevice);
                    } else {//请求权限
                        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(usbDevice, mPermissionIntent);
                    }
                    break;
                }
                /*串口连接*/
                case Constant.SERIALPORT_REQUEST_CODE:
                    //获取波特率
                    int baudrate = data.getIntExtra(Constant.SERIALPORTBAUDRATE, 0);
                    //获取串口号
                    String path = data.getStringExtra(Constant.SERIALPORTPATH);

                    if (baudrate != 0 && !TextUtils.isEmpty(path)) {
                        //初始化DeviceConnFactoryManager
                        new DeviceConnFactoryManager.Build()
                                .setId(baudrate + "")
                                //设置连接方式
                                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.SERIAL_PORT)
                                //设置波特率
                                .setBaudrate(baudrate)
                                //设置串口号
                                .setSerialPort(path)
                                .build();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    id = msg.arg1;
                    if (deviceConnFactoryManagers.get(id) == null) {
                        Toast.makeText(ConnMoreDevicesActivity.this, "请先配置好端口信息", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (deviceConnFactoryManagers.get(id).getConnState()) {
                        deviceConnFactoryManagers.get(id).closePort();
                    } else {
                        ThreadPool.getInstantiation().addSerialTask(threadFactoryBuilder.newThread(new Runnable() {
                            @Override
                            public void run() {
                                deviceConnFactoryManagers.get(id).openPort();
                            }
                        }));
                    }
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString("Ip");
                    String strPort = msg.getData().getString("Port");
                    //初始化端口信息
                    new DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp(strIp)
                            //设置端口ID（主要用于连接多设备）
                            .setId(strIp)
                            //设置连接的热点端口号
                            .setPort(Integer.parseInt(strPort))
                            .build();
                    lists.get(id).put(CONN_METHOD, "WIFI");
                    lists.get(id).put(ListViewAdapter.INFO, getPortParamInfoString(deviceConnFactoryManagers.get(id)));
                    adapter.notifyDataSetChanged();
                    break;
                case Constant.CONN_STATE_DISCONN://断开连接

                    break;
                default:
                    break;
            }
        }
    };


    /**
     * usb连接
     *
     * @param usbDevice
     */
    private void usbConn(UsbDevice usbDevice) {
        new DeviceConnFactoryManager.Build()
                .setId(usbDevice.getSerialNumber())
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(usbDevice)
                .setContext(this)
                .build();
        lists.get(id).put(CONN_METHOD, "USB");
        lists.get(id).put(ListViewAdapter.INFO, getPortParamInfoString(deviceConnFactoryManagers.get(id)));
        adapter.notifyDataSetChanged();
    }

    /**
     * 多设备同步打印
     */
    public void btnSynchronousPrint(View view) {
        int device = 0;
        for (int i = 0; i < 4; i++) {
            final int id = i;
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                    !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                device++;
                if (device == 4) {
                    Utils.toast(this, getString(R.string.str_cann_printer));
                }
                continue;
            }
            threadPool = ThreadPool.getInstantiation();
            threadPool.addSerialTask(new Runnable() {
                @Override
                public void run() {
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getCPCL());
                    } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getLabel());
                    } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getReceipt());
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}