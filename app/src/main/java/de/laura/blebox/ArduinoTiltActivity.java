package de.laura.blebox;

import static de.laura.blebox.ActionMenuActivity.dev;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ArduinoTiltActivity extends AppCompatActivity implements SensorEventListener {
    BluetoothGatt gatt;
    BluetoothGattCharacteristic characteristic;
    SensorManager sensorManager;
    Sensor rot;

    public static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            Toast.makeText(this, R.string.sensor_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }

        rot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

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
                    // handle value
                }
            }
        });

        gatt.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rot, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (characteristic == null) return;

        if (event.sensor == rot) {
            float[] rotationVector = event.values;
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

            float[] orientation = new float[3];

            SensorManager.getOrientation(rotationMatrix, orientation);

            byte r = (byte) ((Math.toDegrees(orientation[1]) + 90) / 360 * 255);
            characteristic.setValue(new byte[]{r});
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                gatt.writeCharacteristic(characteristic);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}