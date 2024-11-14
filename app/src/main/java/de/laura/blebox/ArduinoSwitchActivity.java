package de.laura.blebox;

import static de.laura.blebox.ActionMenu.dev;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.UUID;

public class ArduinoSwitchActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    BluetoothGatt gatt;
    BluetoothGattCharacteristic characteristic;
    public SwitchCompat switch1;

    public static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        switch1 = findViewById(R.id.switch_switch);
        switch1.setOnCheckedChangeListener(this);

        gatt =  dev.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    finish();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service =  gatt.getService(SERVICE_UUID);
                    if (service == null) return;

                    characteristic =  service.getCharacteristic(CHARACTERISTIC_UUID);
                }
            }
        });

        gatt.connect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == switch1.getId()) {
            characteristic.setValue(isChecked ? "1" : "0");
            gatt.writeCharacteristic(characteristic);
        }
    }
}