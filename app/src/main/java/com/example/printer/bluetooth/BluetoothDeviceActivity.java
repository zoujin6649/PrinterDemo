package com.example.printer.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lib_printer.DeviceConnFactoryManager;
import com.example.lib_printer.PrintContent;
import com.example.lib_printer.PrinterCommand;
import com.example.lib_printer.ThreadFactoryBuilder;
import com.example.lib_printer.ThreadPool;
import com.example.printer.DeviceAdapter;
import com.example.printer.R;
import com.example.printer.base.BaseActivity;
import com.example.printer.base.BasePresenter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.example.lib_printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

/**
 * 权限：android.permission.BLUETOOTH //在得到默认蓝牙适配器时需要，BluetoothAdapter.getDefaultAdapter()
 * 权限：android.permission.BLUETOOTH_ADMIN //在BluetoothAdapter.enable()或者BluetoothAdapter.disable()时需要使用
 * 权限：android.permission.ACCESS_FINE_LOCATION //蓝牙扫描可用于收集用户的位置信息。此类信息可能来自用户自己的设备，
 * 以及在商店和交通设施等位置使用的蓝牙信标。Android 9及以下的版本，可用android.permission.ACCESS_COARSE_LOCATION替代
 * <p>
 * BluetoothAdapter:蓝牙适配器，所有蓝牙交互的入口，使用它可以发现其他蓝牙设备，查询已配对的设备列表，
 * 使用一个已知的MAC地址来实例化一个BluetoothDevice，以及创建一个BluetoothServerSocket来为监听与其他设备的通信。
 * BluetoothDevice:代表一个远程蓝牙设备，使用这个来请求一个与远程设备的BluetoothSocket连接，或者查询关于设备名称、地址、类和连接状态等设备信息
 * BluetoothSocket:代表一个蓝牙Socket的接口（和TCP socket类似）。这是一个连接店，它允许一个应用与其他蓝牙设备通过InputStream和OutputStream交互数据
 * BluetoothServerSocket:代表一个开放的服务器Socket，它监听接受的请求（与TCP ServerSocket类似）。为了连接两台Android设备，
 * 一个设备必须使用这个类开启一个服务器socket。当一个远程蓝牙设备开始一个和该设备的连接请求，
 * BluetoothServerSocket将会返回一个已连接的BluetoothSocket，接受该连接。
 * <p>
 * 蓝牙配对是建立连接的基础和前提。经过配对，设备之前以PIN码建立约定的link key用于产生初始认证码，以用于以后建立的连接，
 * 如果不配对，两个设备之间便无法建立认证关系，无法进行连接及其之后的操作
 * Created by claire on 2020/9/2.
 */
public class BluetoothDeviceActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BLE = 2;
    private static final int REQUEST_DISCOVERABLE_BLE = 3;
    private static final String TAG = BluetoothDeviceActivity.class.getName();

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();

    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (TextUtils.isEmpty(action) || bluetoothDevice == null) {
                return;
            }
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.e("TAG", "正在搜索附近的蓝牙设备");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.e("TAG", "搜索结束");
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.e("TAG", "与" + bluetoothDevice.getName() + "蓝牙已连接");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.e("TAG", "与" + bluetoothDevice.getName() + "蓝牙连接已结束");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        Map<String, Object> map = new HashMap<>();
                        String info = getString(R.string.str_bluetooth) + "  " + getString(R.string.str_address) + bluetoothDevice.getAddress();
                        map.put(DeviceAdapter.TITLE, bluetoothDevice.getName());
                        map.put(DeviceAdapter.MAC_ADDRESS, bluetoothDevice.getAddress());
                        map.put(DeviceAdapter.CONN_METHOD, DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH.toString());
                        map.put(DeviceAdapter.BT_ENABLE, "enable");
                        map.put(DeviceAdapter.STATUS, getString(R.string.connect));
                        if (!lists.contains(map)) {
                            lists.add(map);
                            //初始化话DeviceConnFactoryManager
                            new DeviceConnFactoryManager.Build()
                                    //设置连接方式
                                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                                    //设置连接的蓝牙mac地址
                                    .setMacAddress(bluetoothDevice.getAddress())
                                    .setId(bluetoothDevice.getAddress())
                                    .build();
                            adapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_OFF:
                            showToast("蓝牙已关闭");
                            finish();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            showToast("蓝牙已开启");
                            switch (bluetoothAdapter.getScanMode()) {
                                case BluetoothAdapter.SCAN_MODE_NONE:
                                    //无功能状态，查询扫描和页面扫描都无效，该状态下蓝牙模块既不能扫描其他设备，也不可见
                                    //请求开启可见
                                    Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                                    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                                    startActivityForResult(discoveryIntent, REQUEST_DISCOVERABLE_BLE);
                                    break;
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                                    //扫描状态，查询扫描失效，页面扫描有效，该状态下蓝牙模块可以扫描其他设备，从可见性来说只对已配对的蓝牙设备可见，只有配对的设备才能主动连接本设备
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                                    //可见状态，查询扫描和页面扫面都有效
                                    setPairingDevice();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            scanDevice();
                                        }
                                    }, 1000);

                                    break;
                            }
                            break;
                    }
                    break;
            }
        }
    };


    private Map<String, DeviceConnFactoryManager> deviceConnFactoryManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
    private byte[] sendCommand;
    private ThreadFactoryBuilder threadFactoryBuilder;
    private ThreadPool threadPool = null;
    private List<Map<String, Object>> lists = new ArrayList() {
        @Override
        public boolean contains(@Nullable Object o) {
            Map<String, Object> thisMap = (Map<String, Object>) o;
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i) != null) {
                    if (((Map<String, Object>) this.get(i)).get(DeviceAdapter.MAC_ADDRESS).equals(thisMap.get(DeviceAdapter.MAC_ADDRESS))) {
                        return true;
                    }
                }
            }
            return false;
        }
    };
    private DeviceAdapter adapter;

    /**
     * 广播监听
     */
    private BroadcastReceiver connStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    String id = intent.getStringExtra(DeviceConnFactoryManager.DEVICE_ID);
                    Log.e(TAG, "收到广播id:" + id);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            for (Map<String, Object> map : lists) {
                                if (map.get(DeviceAdapter.MAC_ADDRESS).equals(id)) {
                                    showToast(getString(R.string.str_conn_state_disconnect));
                                    map.put(DeviceAdapter.STATUS, getString(R.string.connect));
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            for (Map<String, Object> map : lists) {
                                if (map.get(DeviceAdapter.MAC_ADDRESS).equals(id)) {
                                    showToast(getString(R.string.connecting));
                                    map.put(DeviceAdapter.STATUS, getString(R.string.str_connecting));
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            for (Map<String, Object> map : lists) {
                                if (map.get(DeviceAdapter.MAC_ADDRESS).equals(id)) {
                                    showToast(getString(R.string.str_conn_state_connected));
                                    map.put(DeviceAdapter.STATUS, getString(R.string.str_disconn));
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            break;
                        case CONN_STATE_FAILED:
                            for (Map<String, Object> map : lists) {
                                if (map.get(DeviceAdapter.MAC_ADDRESS).equals(id)) {
                                    showToast(getString(R.string.str_conn_fail));
                                    map.put(DeviceAdapter.STATUS, getString(R.string.connect));
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            break;
                    }
                    break;
            }
        }
    };


    @Override
    protected void initView(Bundle savedInstanceState) {
        //Android 10，除了开启蓝牙，还需要开启GPS功能才行，不然不能搜索和连接其他蓝牙设备
        checkPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showToast("请您先开启gps，否则蓝牙不可用");
                return;
            }
        }

        threadPool = ThreadPool.getInstantiation();
        threadFactoryBuilder = new ThreadFactoryBuilder("ConnMoreDevicesActivity");

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(lists);
        adapter.setOnConnClickListener(new BluetoothDeviceAdapter.OnConnClickListener() {
            @Override
            public void onConnClick(int position) {
                if (deviceConnFactoryManagers.get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).getConnState()) {
                    //断开
                    deviceConnFactoryManagers.get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).closePort();
                } else {
                    //连接
                    ThreadPool.getInstantiation().addSerialTask(threadFactoryBuilder.newThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceConnFactoryManagers.get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).openPort();
                        }
                    }));
                }
            }
        });
        adapter.setOnPrintClickListener(new BluetoothDeviceAdapter.onPrintClickListener() {
            @Override
            public void onPrintClick(int position) {
                if (!deviceConnFactoryManagers.get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).getConnState()) {
                    showToast(getString(R.string.str_cann_printer));
                    return;
                }
                threadPool = ThreadPool.getInstantiation();
                threadPool.addSerialTask(new Runnable() {
                    @Override
                    public void run() {
                        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).sendDataImmediately(PrintContent.getCPCL());
                        } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).getCurrentPrinterCommand() == PrinterCommand.TSC) {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).sendDataImmediately(PrintContent.getLabel());
                        } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).getCurrentPrinterCommand() == PrinterCommand.ESC) {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(lists.get(position).get(DeviceAdapter.MAC_ADDRESS)).sendDataImmediately(PrintContent.getReceipt());
                        }
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        registerConnStatusReceiver();
        registerDiscoveryReceiver();

        initBluetoothAdapter();
        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    //请求开启蓝牙
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BLE);
                } else {
                    setPairingDevice();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanDevice();
                        }
                    }, 1000);
                }
            }
        });
    }

    private void initBluetoothAdapter() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        //3.获得本地蓝牙适配器
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("当前设备不支持蓝牙");
            finish();
            return;
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                //请求开启蓝牙
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLE);
            } else {
                switch (bluetoothAdapter.getScanMode()) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        //无功能状态，查询扫描和页面扫描都无效，该状态下蓝牙模块既不能扫描其他设备，也不可见
                        //请求开启可见
                        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivityForResult(discoveryIntent, REQUEST_DISCOVERABLE_BLE);
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        //扫描状态，查询扫描失效，页面扫描有效，该状态下蓝牙模块可以扫描其他设备，从可见性来说只对已配对的蓝牙设备可见，只有配对的设备才能主动连接本设备
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        //可见状态，查询扫描和页面扫面都有效
                        setPairingDevice();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scanDevice();
                            }
                        }, 1000);

                        break;
                }
            }
        }
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_DISCOVERABLE_BLE:
                if (resultCode == RESULT_OK) {
                    setPairingDevice();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanDevice();
                        }
                    }, 1000);
                } else {
                    showToast("请开启可见性");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        showToast("请授予位置权限");
                        finish();
                        return;
                    }
                }
                break;
        }
    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected int getResLayout() {
        return R.layout.activity_bluetooth_device;
    }

    @Override
    protected BasePresenter getPresenter() {
        return null;
    }


    private void setPairingDevice() {
        lists.clear();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            //存在已配对过的设备
            BluetoothDevice[] bluetoothDevices = devices.toArray(new BluetoothDevice[devices.size()]);
            for (int i = 0; i < bluetoothDevices.length; i++) {
                BluetoothDevice device = bluetoothDevices[i];
                Map<String, Object> map = new HashMap<>();
                map.put(DeviceAdapter.TITLE, device.getName());
                map.put(DeviceAdapter.MAC_ADDRESS, device.getAddress());
                map.put(DeviceAdapter.CONN_METHOD, DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH.toString());
                map.put(DeviceAdapter.BT_ENABLE, "enable");
                map.put(DeviceAdapter.STATUS, getString(R.string.connect));
                if (!lists.contains(map)) {
                    lists.add(map);
                    //初始化话DeviceConnFactoryManager
                    new DeviceConnFactoryManager.Build()
                            //设置连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            //设置连接的蓝牙mac地址
                            .setMacAddress(device.getAddress())
                            .setId(device.getAddress())
                            .build();
                }
            }
            adapter.notifyDataSetChanged();
        }
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

    private void scanDevice() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void registerConnStatusReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        filter.addAction(ACTION_USB_DEVICE_DETACHED);
        registerReceiver(connStatusReceiver, filter);
    }


    private void registerDiscoveryReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//扫描开始
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态改变监听
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//已连接
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//断开连接
        registerReceiver(discoveryReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(discoveryReceiver);
        unregisterReceiver(connStatusReceiver);
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * 配对蓝牙
     *
     * @param bluetoothDevice
     */
    private void pinTargetDevice(BluetoothDevice bluetoothDevice) {
        //在配对之前，停止搜索
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (bluetoothDevice.getBondState() != bluetoothDevice.BOND_BONDED) {
            try {
                Method createBond = BluetoothDevice.class.getMethod("createBond");
                Boolean returnValue = (Boolean) createBond.invoke(bluetoothDevice);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param bluetoothDevice
     */
    private void cancelPinTargetDevice(BluetoothDevice bluetoothDevice) {
        try {
            Method removeBond = bluetoothDevice.getClass().getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBond.invoke(bluetoothDevice);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
