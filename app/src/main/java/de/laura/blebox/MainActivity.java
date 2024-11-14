package de.laura.blebox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button bscan, select;
    TextView logView;
    Spinner spinner;

    HashMap<String, BluetoothDevice> devices;

    BluetoothManager bluetoothManager;
    BluetoothLeScanner bluetoothLeScanner;
    ScanCallback callback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice dev = result.getDevice();
            if (dev.getName() == null) return;
            devices.put(dev.getAddress(), dev);

            redraw();
        }
    };

    boolean scanning = false;

    public boolean ensurePermissions() {
        String[] perms = Build.VERSION.SDK_INT >= 31 ? new String[]{android.Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION} : new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        ActivityCompat.requestPermissions(this, perms, 0);
        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) return false;
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    public void redraw() {
        String text = devices.size() == 1 ? getResources().getString(R.string.log_1_device) : devices.size() + getResources().getString(R.string.log_n_devices);
        for (String addr : devices.keySet()) {
            text += addr + ": " + devices.get(addr).getName() + "\n";
        }
        logView.setText(text);

        String puuid = (String) spinner.getSelectedItem();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, devices.keySet().stream().map(uuid -> devices.get(uuid).getName() + " " + uuid).collect(Collectors.toSet()).toArray(new String[]{}));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (puuid != null) {
            for (int i = 0; i < devices.size(); i++) {
                if (puuid.equals(spinner.getItemAtPosition(i))) spinner.setSelection(i);
            }
        }
    }

    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_unsupported, Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothManager = (BluetoothManager) getBaseContext().getSystemService(Context.BLUETOOTH_SERVICE);

        bscan = findViewById(R.id.main_scan);
        bscan.setOnClickListener(this);

        select = findViewById(R.id.main_select);
        select.setOnClickListener(this);

        logView = findViewById(R.id.main_log_view);
        logView.setMovementMethod(new ScrollingMovementMethod());

        spinner = findViewById(R.id.main_device_spinner);
    }

    @SuppressLint("MissingPermission")
    public void toggleScan() {
        if (!ensurePermissions()) return;

        if (scanning) {
            scanning = false;

            bluetoothLeScanner.stopScan(callback);
            return;
        }

        bluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();

        if (bluetoothLeScanner == null) {
            Toast.makeText(this, R.string.no_bt_scanner, Toast.LENGTH_SHORT).show();
        }

        scanning = true;
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.MATCH_MODE_AGGRESSIVE).build();
        devices = new HashMap<>();
        bluetoothLeScanner.startScan(new ArrayList<>(), settings, callback);
    }

    @Override
    public void onClick(View v) {
        if (ensurePermissions()) {
            if (v.getId() == bscan.getId()) {
                toggleScan();
            } else if (v.getId() == select.getId()) {
                if (spinner.getSelectedItemPosition() != -1) {
                    String[] uuidParts = ((String) spinner.getSelectedItem()).split(" ");
                    String uuid = uuidParts[uuidParts.length - 1];

                    BluetoothDevice dev = devices.get(uuid);
                    ActionMenu.dev = dev;
                    Intent intent = new Intent(this, ArduinoSwitchActivity.class);
                    startActivity(intent);
                }
            }
        }

        bscan.setText(scanning ? R.string.stop_scan : R.string.start_scan);
        redraw();
    }
}