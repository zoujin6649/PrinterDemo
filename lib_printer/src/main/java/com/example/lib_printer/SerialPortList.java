package com.example.lib_printer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.gprinter.utils.SerialPortFinder;


/**
 * Created by Administrator
 *
 * @author 猿史森林
 *         Date: 2017/10/14
 *         Class description:
 */
public class SerialPortList extends Activity {

    private SerialPortFinder mSerialPortFinder;
    private String[] entries;
    private String[] entryValues;
    private Spinner spSerialPortPath;
    private Spinner spBaudrate;
    private String path;
    private int selectBaudrate;
    private String[] baudrates;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_serialport);
        baudrates = this.getResources().getStringArray(R.array.baudrate);
        mSerialPortFinder = new SerialPortFinder();
        entries = mSerialPortFinder.getAllDevices();
        //获取串口路径
        entryValues = mSerialPortFinder.getAllDevicesPath();
        initView();
        initListener();
    }

    private void initView() {
        //串口路径初始化
        spSerialPortPath = (Spinner) findViewById(R.id.sp_serialport_path);
        ArrayAdapter arrayAdapter;
        if (entries != null) {
            arrayAdapter = new ArrayAdapter(this, R.layout.sp_tv_item, entries);
        } else {
            arrayAdapter = new ArrayAdapter(this, R.layout.sp_tv_item, new String[]{this.getString(R.string.str_no_serialport)});
        }
        spSerialPortPath.setAdapter(arrayAdapter);
        //波特率数据初始化
        spBaudrate = (Spinner) findViewById(R.id.sp_baudrate);
        ArrayAdapter portAdapter = new ArrayAdapter(this, R.layout.sp_tv_item, this.getResources().getStringArray(R.array.baudrate));
        spBaudrate.setAdapter(portAdapter);
        btnConfirm = (Button) findViewById(R.id.btn_confirm);
    }

    private void initListener() {
        spBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //保存当前选择的波特率
                selectBaudrate = Integer.parseInt(baudrates[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSerialPortPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (entryValues != null) {
                    path = entryValues[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.SERIALPORTPATH, path);
                bundle.putInt(Constant.SERIALPORTBAUDRATE, selectBaudrate);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
