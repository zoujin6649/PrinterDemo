package com.example.lib_printer;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gprinter.command.CpclCommand;
import com.gprinter.command.EscCommand;
import com.gprinter.command.FactoryCommand;
import com.gprinter.command.LabelCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.lib_printer.Constant.ACTION_USB_PERMISSION;
import static com.example.lib_printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.example.lib_printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.example.lib_printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 * Date: 2017/8/2
 * Class description:
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    ArrayList<String> per = new ArrayList<>();
    private UsbManager usbManager;
    private int counts;
    private static final int REQUEST_CODE = 0x004;
    /**
     * 连接状态断开
     */
    private static final int CONN_STATE_DISCONN = 0x007;
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;


    /**
     * ESC查询打印机实时状态指令
     */
    private byte[] esc = {0x10, 0x04, 0x02};
    /**
     * CPCL查询打印机实时状态指令
     */
    private byte[] cpcl = {0x1b, 0x68};
    /**
     * TSC查询打印机状态指令
     */
    private byte[] tsc = {0x1b, '!', '?'};

    private static final int CONN_MOST_DEVICES = 0x11;
    private static final int CONN_PRINTER = 0x12;
    private PendingIntent mPermissionIntent;
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
    };
    private String usbName;
    private TextView tvConnState;
    private ThreadPool threadPool;
    /**
     * 判断打印机所使用指令是否是ESC指令
     */
    private int id = 0;
    private EditText etPrintCounts;
    private Spinner mode_sp;
    private int printcount = 0;
    private boolean continuityprint = false;
    private CheckWifiConnThread checkWifiConnThread;//wifi连接线程监听

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        checkPermission();
        requestPermission();
        tvConnState = (TextView) findViewById(R.id.tv_connState);
        etPrintCounts = (EditText) findViewById(R.id.et_print_counts);
        initsp();
        initBroadcast();
    }

    /**
     * 注册广播
     * Registration broadcast
     */
    private void initBroadcast() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);//USB访问权限广播
        filter.addAction(ACTION_USB_DEVICE_DETACHED);//USB线拔出
        filter.addAction(ACTION_QUERY_PRINTER_STATE);//查询打印机缓冲区状态广播，用于一票一控
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);//与打印机连接状态
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);//USB线插入
        registerReceiver(receiver, filter);
    }

    private void initsp() {
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.str_cpclmode));
        list.add(getString(R.string.str_tscmode));
        list.add(getString(R.string.str_escmode));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mode_sp = (Spinner) findViewById(R.id.mode_sp);
        mode_sp.setAdapter(adapter);
    }


    private void checkPermission() {
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                per.add(permission);
            }
        }
    }

    private void requestPermission() {
        if (per.size() > 0) {
            String[] p = new String[per.size()];
            ActivityCompat.requestPermissions(this, per.toArray(p), REQUEST_CODE);
        }
    }

    /**
     * 蓝牙连接
     * 安卓系统6.0+，搜索蓝牙需要用户授予访问定位权限，否则将搜索不到蓝牙
     * Bluetooth connection
     * Android 6.0+, search for Bluetooth requires users to grant access to the location, otherwise Bluetooth will not be searched
     */
    public void btnBluetoothConn(View view) {
        startActivityForResult(new Intent(this, BluetoothDeviceList.class), Constant.BLUETOOTH_REQUEST_CODE);
    }

    /**
     * USB连接
     * 访问USB设备需要客户授予访问权限
     * USB connection
     * Access to USB devices requires client access
     *
     * @param view
     */
    public void btnUsbConn(View view) {
        startActivityForResult(new Intent(this, UsbDeviceList.class), Constant.USB_REQUEST_CODE);
    }

    /**
     * 连接多设备
     * Connect multiple devices
     *
     * @param view
     */
    public void btnMoreDevices(View view) {
        unregisterReceiver(receiver);//进入多设备取消广播监听
        startActivityForResult(new Intent(this, ConnMoreDevicesActivity.class), CONN_MOST_DEVICES);
    }

    /**
     * 获取打印机ip信息说明：
     * 打印打印机自检页，查看当前打印机网络信息，步骤如下
     * 1.打印机关机
     * 2.按住FEED键（不松手），此时开启打印机
     * 3..红灯灭后，松手feed键，此过程打印机3-5秒，正常则打印一张自检页，可查看ip
     * 若打印机打印出Hexadecimal Dump字样，则进去16进制模式，需要重试上述步骤
     * <p>
     * 安卓设备需要跟打印机处于局域网内，实现通讯
     * 如需要设置打印机ip，请联系客服索要修改工具即可
     * <p>
     * WIFI连接：
     * 连接打印机wifi，打印打印机自检页或接口，查看当前ip
     * <p>
     * 网口：
     * 1.安卓机器与打印机用网线连接
     * 2.进入安卓系统设置以太网，打印机与安卓ip段处于局域网内（不能一样）
     * <p>
     * Get the printer ip information description:
     * Print the printer self-test page to view the current printer network information, the steps are as follows
     * 1. Printer shutdown
     * 2. Press and hold the FEED button (do not let go), then turn on the printer
     * 3. After the red light is off, release the feed button, the process printer 3-5 seconds, normal print a self-test page, you can view ip
     * If the printer prints the Hexadecimal Dump, enter the hexadecimal mode and you need to retry the above steps.
     * <p>
     * Android devices need to be in the LAN with the printer to achieve communication
     * If you need to set the printer ip, please contact customer service to request the modification tool.
     * <p>
     * WIFI connection:
     * Connect printer wifi, print printer self-test page or interface, view current ip
     * <p>
     * Network port:
     * 1. Android machine and printer are connected by network cable
     * 2. Enter the Android system to set the Ethernet, the printer and the Android ip segment are in the LAN (not the same)
     *
     * @param view
     */
    public void btnWifiConn(View view) {
        WifiParameterConfigDialog wifiParameterConfigDialog = new WifiParameterConfigDialog(this, mHandler);
        wifiParameterConfigDialog.show();
    }

    /**
     * 串口连接
     *
     * @param view
     */
    public void btnSerialPortConn(View view) {
        startActivityForResult(new Intent(this, SerialPortList.class), Constant.SERIALPORT_REQUEST_CODE);
    }

    /**
     * 断开连接
     *
     * @param view
     */
    public void btnDisConn(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
    }

    /**
     * 打印票据例子
     *
     * @param view
     */
    public void btnReceiptPrint(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getReceipt());
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    /**
     * 打印标签例子
     *
     * @param view
     */
    public void btnLabelPrint(View view) {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getLabel());
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    public void btnLabelMatrix(View view) {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getNewCommandToPrintQrcode());
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    /**
     * 打印标签长图
     * 若不支持该指令则打印空白
     *
     * @param view
     */
    public void btnlabelPhoto(View view) {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.printViewPhoto(bitmap));
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    /**
     * 打印面单例子
     *
     * @param view
     */
    public void btnCpclPrint(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getCPCL());
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }


    /**
     * 打印自检页
     *
     * @param view
     */
    public void btnPrintSelftest(View view) {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(FactoryCommand.printSelfTest(FactoryCommand.printerMode.TSC));
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(FactoryCommand.printSelfTest(FactoryCommand.printerMode.ESC));
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    /**
     * 打印XML布局文件
     *
     * @param view
     */
    public void btnPrintXml(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
            return;
        }

        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {

                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                    CpclCommand cpcl = new CpclCommand();
                    cpcl.addInitializePrinter(1500, 1);
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    cpcl.addCGraphics(0, 0, 576, PrintContent.getBitmap(MainActivity.this));
                    cpcl.addPrint();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(cpcl.getCommand());
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    LabelCommand labelCommand = new LabelCommand();
                    labelCommand.addSize(80, 180);
                    labelCommand.addCls();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    labelCommand.addBitmap(0, 0, 576, PrintContent.getBitmap(MainActivity.this));
                    labelCommand.addPrint(1);
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(labelCommand.getCommand());
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    EscCommand esc = new EscCommand();
                    esc.addInitializePrinter();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    esc.addRastBitImage(PrintContent.getBitmap(MainActivity.this), 576, 0);
                    esc.addPrintAndLineFeed();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(esc.getCommand());
                }

            }
        });
    }


    /**
     * 打印机状态查询，部分打印机没有返回值，则无法收到返回，
     *
     * @param view
     */
    public void btnPrinterState(View view) {
        //打印机状态查询
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        ThreadPool.getInstantiation().addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(esc);
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(tsc);
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(cpcl);
                }
            }
        });
    }


    /**
     * 切换打印模式
     *
     * @param view
     */
    public void btnModeChange(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        int sp_no = mode_sp.getSelectedItemPosition();
        byte[] bytes = null;
        switch (sp_no) {
            case 0:
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                    tip(String.format(getString(R.string.str_mode_tip), getString(R.string.str_cpclmode)));
                    return;
                } else {
                    bytes = FactoryCommand.changPrinterMode(FactoryCommand.printerMode.CPCL);
                }
                break;
            case 1:
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    tip(String.format(getString(R.string.str_mode_tip), getString(R.string.str_tscmode)));
                    return;
                } else {
                    bytes = FactoryCommand.changPrinterMode(FactoryCommand.printerMode.TSC);
                }
                break;
            case 2:
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    tip(String.format(getString(R.string.str_mode_tip), getString(R.string.str_escmode)));
                    return;
                } else {
                    bytes = FactoryCommand.changPrinterMode(FactoryCommand.printerMode.ESC);
                }
                break;
        }
        threadPool = ThreadPool.getInstantiation();
        final byte[] finalBytes = bytes;
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {//发送切换打印机模式后会断开连接，如果切换模式成功，打印机蜂鸣器会响一声，打印机关机，需手动开启
                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendByteDataImmediately(finalBytes);
                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).closePort();
            }
        });

    }

    /**
     * 提示
     *
     * @param msg
     */
    private void tip(String msg) {
        Message message = new Message();
        message.obj = msg;
        message.what = Constant.tip;
        mHandler.sendMessage(message);
    }

    /**
     * 连续打印
     *
     * @param view
     */
    public void btnReceiptAndLabelContinuityPrint(View view) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        if (etPrintCounts.getText().toString().trim().isEmpty()) {
            Utils.toast(this, getString(R.string.str_continuity_count));
            return;
        }
        counts = Integer.parseInt(etPrintCounts.getText().toString().trim());
        printcount = 0;
        continuityprint = true;
        sendContinuityPrint();
    }

    private void sendContinuityPrint() {
        ThreadPool.getInstantiation().addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) != null
                        && DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                    ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder("MainActivity_sendContinuity_Timer");
                    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactoryBuilder);
                    scheduledExecutorService.schedule(threadFactoryBuilder.newThread(new Runnable() {
                        @Override
                        public void run() {
                            counts--;
                            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getReceipt());
                            } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getLabel());
                            } else {
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).sendDataImmediately(PrintContent.getCPCL());
                            }
                        }
                    }), 1000, TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                /*蓝牙连接*/
                case Constant.BLUETOOTH_REQUEST_CODE: {
                    closeport();
                    /*获取蓝牙mac地址*/
                    String macAddress = data.getStringExtra(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
                    //初始化话DeviceConnFactoryManager
                    new DeviceConnFactoryManager.Build()
                            .setId(macAddress)
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(macAddress)
                            .build();
                    //打开端口
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addSerialTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).openPort();
                        }
                    });

                    break;
                }
                /*USB连接*/
                case Constant.USB_REQUEST_CODE: {
                    closeport();
                    //获取USB设备名
                    usbName = data.getStringExtra(UsbDeviceList.USB_NAME);
                    //通过USB设备名找到USB设备
                    UsbDevice usbDevice = Utils.getUsbDeviceFromName(MainActivity.this, usbName);
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
                    closeport();
                    //获取波特率
                    int baudrate = data.getIntExtra(Constant.SERIALPORTBAUDRATE, 0);
                    //获取串口号
                    String path = data.getStringExtra(Constant.SERIALPORTPATH);
                    if (baudrate != 0 && !TextUtils.isEmpty(path)) {
                        //初始化DeviceConnFactoryManager
                        new DeviceConnFactoryManager.Build()
                                //设置连接方式
                                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.SERIAL_PORT)
                                .setId(path)
                                //设置波特率
                                .setBaudrate(baudrate)
                                //设置串口号
                                .setSerialPort(path)
                                .build();
                        //打开端口
                        //打开端口
                        threadPool = ThreadPool.getInstantiation();
                        threadPool.addSerialTask(new Runnable() {
                            @Override
                            public void run() {
                                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).openPort();
                            }
                        });
                    }
                    break;
                /*多设备*/
                case CONN_MOST_DEVICES:
                    initBroadcast();//注册广播监听
                    id = data.getIntExtra("id", -1);
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) != null &&
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
                        tvConnState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                    } else {
                        tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 重新连接回收上次连接的对象，避免内存泄漏
     */
    private void closeport() {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).mPort != null) {
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).reader.cancel();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).mPort.closePort();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).mPort = null;
        }
    }

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
        DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).openPort();
    }

    /**
     * 停止连续打印
     */
    public void btnStopContinuityPrint(View v) {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id) == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).getConnState()) {
            Utils.toast(this, getString(R.string.str_cann_printer));
            return;
        }
        if (counts != 0) {
            counts = 0;
            Utils.toast(this, getString(R.string.str_stop_continuityprint_success));
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //USB请求访问权限
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {//用户点击授权
                                usbConn(device);
                            }
                        } else {//用户点击不授权,则无权限访问USB
                            Log.e(TAG, "No access to USB");
                        }
                    }
                    break;
                //Usb连接断开广播
                case ACTION_USB_DEVICE_DETACHED:
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (usbDevice.equals(DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).usbDevice())) {
                        mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    }
                    break;
                //连接状态
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            if (id == deviceId) {
                                Log.e(TAG, "connection is lost");
                                tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            tvConnState.setText(getString(R.string.str_conn_state_connecting));
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            tvConnState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast(MainActivity.this, getString(R.string.str_conn_fail));
                            //wificonn=false;
                            tvConnState.setText(getString(R.string.str_conn_state_disconnect));
                            break;
                        default:
                            break;
                    }
                    break;
                //连续打印，一票一控，防止打印机乱码
                case ACTION_QUERY_PRINTER_STATE:
                    if (counts >= 0) {
                        if (continuityprint) {
                            printcount++;
                            Utils.toast(MainActivity.this, getString(R.string.str_continuityprinter) + " " + printcount);
                        }
                        if (counts != 0) {
                            sendContinuityPrint();
                        } else {
                            continuityprint = false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN://断开连接
                    DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id);
                    if (deviceConnFactoryManager != null && deviceConnFactoryManager.getConnState()) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).closePort();
                        Utils.toast(MainActivity.this, getString(R.string.str_disconnect_success));
                    }
                    break;
                case PRINTER_COMMAND_ERROR://打印机指令错误
                    Utils.toast(MainActivity.this, getString(R.string.str_choice_printer_command));
                    break;
                case CONN_PRINTER://未连接打印机
                    Utils.toast(MainActivity.this, getString(R.string.str_cann_printer));
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
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addSerialTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).openPort();
                        }
                    });
                    break;
                case CheckWifiConnThread.PING_SUCCESS://WIfi连接成功\
                    Log.e(TAG, "wifi connect success!");
                    break;
                case CheckWifiConnThread.PING_FAIL://WIfI断开连接
                    Log.e(TAG, "wifi connect fail!");
                    Utils.toast(MainActivity.this, getString(R.string.disconnect));
                    checkWifiConnThread.cancel();
                    checkWifiConnThread = null;
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case Constant.tip:
                    String str = (String) msg.obj;
                    Utils.toast(MainActivity.this, str);
                    break;
                default:
                    new DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp("192.168.2.227")
                            //设置端口ID（主要用于连接多设备）
                            .setId("192.168.2.227")
                            //设置连接的热点端口号
                            .setPort(9100)
                            .build();
                    threadPool.addSerialTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id).openPort();
                        }
                    });
                    break;
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        //获取连接对象是否连接
        Map<String, DeviceConnFactoryManager> deviceConnFactoryManagers;
        deviceConnFactoryManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
        for (int i = 0; i < 4; i++) {
            if (deviceConnFactoryManagers.get(i) != null && deviceConnFactoryManagers.get(i).getConnState()) {
                tvConnState.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                break;
            } else {
                tvConnState.setText(getString(R.string.str_conn_state_disconnect));
            }
        }
    }

    /**
     * 获取当前连接设备信息
     *
     * @return
     */
    private String getConnDeviceInfo() {
        String str = "";
        DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(id);
        if (deviceConnFactoryManager != null
                && deviceConnFactoryManager.getConnState()) {
            if ("USB".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "USB\n";
                str += "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
            } else if ("WIFI".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "WIFI\n";
                str += "IP: " + deviceConnFactoryManager.getIp() + "\t";
                str += "Port: " + deviceConnFactoryManager.getPort();
                checkWifiConnThread = new CheckWifiConnThread(deviceConnFactoryManager.getIp(), mHandler);//开启监听WiFi线程
                checkWifiConnThread.start();
            } else if ("BLUETOOTH".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "BLUETOOTH\n";
                str += "MacAddress: " + deviceConnFactoryManager.getMacAddress();
            } else if ("SERIAL_PORT".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "SERIAL_PORT\n";
                str += "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
                str += "Baudrate: " + deviceConnFactoryManager.getBaudrate();
            }
        }
        return str;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        unregisterReceiver(receiver);
        if (usbManager != null) {
            usbManager = null;
        }
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
        }
    }
}