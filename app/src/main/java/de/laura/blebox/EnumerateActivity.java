package de.laura.blebox;

import static de.laura.blebox.ActionMenuActivity.dev;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnumerateActivity extends AppCompatActivity {
    BluetoothGatt gatt;

    TextView log;

    @SuppressLint({"MissingPermission", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enumerate);

        log = findViewById(R.id.enumerate_log);

        gatt = dev.connectGatt(this, true, new BluetoothGattCallback() {
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
                    StringBuilder text = new StringBuilder(getResources().getString(R.string.discovered_services) + "\n");
                    for (BluetoothGattService service : gatt.getServices()) {
                        text.append(service.getUuid().toString()).append(":\n");

                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            text.append("    ").append(characteristic.getUuid()).append("\n");
                        }
                    }

                    System.out.println(text);
                    new Handler(Looper.getMainLooper()).post(() -> log.setText(text.toString()));
                }
            }
        });

        gatt.connect();
    }
}