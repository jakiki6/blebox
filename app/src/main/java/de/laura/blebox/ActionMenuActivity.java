package de.laura.blebox;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ActionMenuActivity extends AppCompatActivity implements View.OnClickListener {
    public static BluetoothDevice dev;

    Button actionSwitch, actionEnumerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_menu);

        actionSwitch = findViewById(R.id.action_switch);
        actionEnumerate = findViewById(R.id.action_enumerate);

        actionSwitch.setOnClickListener(this);
        actionEnumerate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == actionSwitch.getId()) {
            startActivity(new Intent(this, ArduinoSwitchActivity.class));
        } else if (v.getId() == actionEnumerate.getId()) {
            startActivity(new Intent(this, EnumerateActivity.class));
        }
    }
}