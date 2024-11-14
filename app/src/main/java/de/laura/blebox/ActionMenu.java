package de.laura.blebox;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ActionMenu extends AppCompatActivity implements View.OnClickListener {
    public static BluetoothDevice dev;

    Button actionSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_menu);

        actionSwitch = findViewById(R.id.action_switch);
        actionSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == actionSwitch.getId()) {
            startActivity(new Intent(this, ArduinoSwitchActivity.class));
        }
    }
}