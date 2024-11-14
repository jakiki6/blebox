package com.example.bletest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;

public class DeviceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public static BluetoothDevice dev;
    public static BluetoothGatt gyatt;
    public static BluetoothGattCharacteristic characteristic;
    public Switch switch1;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);

        gyatt =  dev.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service =  gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                    if (service == null) return;

                    characteristic =  service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                    if (characteristic == null) return;
                }
            }
        });

        gyatt.connect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == switch1.getId()) {
            characteristic.setValue(isChecked ? "1" : "0");
            gyatt.writeCharacteristic(characteristic);
        }
    }
}