package de.laura.blebox;

import static de.laura.blebox.ActionMenuActivity.dev;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.nio.charset.StandardCharsets;
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

                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        gatt.discoverServices();
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
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

            @Override
            public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);

                if (characteristic.getService().getUuid().equals(SERVICE_UUID) && characteristic.getUuid().equals(CHARACTERISTIC_UUID)) {
                    Toast.makeText(ArduinoSwitchActivity.this, "Text: " + new String(value, StandardCharsets.UTF_8), Toast.LENGTH_SHORT).show();
                }
            }
        });

        gatt.connect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == switch1.getId()) {
            if (characteristic == null) {
                Toast.makeText(this, R.string.service_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }

            characteristic.setValue(isChecked ? "1" : "0");
            gatt.writeCharacteristic(characteristic);
        }
    }
}