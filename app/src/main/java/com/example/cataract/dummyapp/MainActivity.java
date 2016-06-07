package com.example.cataract.dummyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = new BluetoothManager(this, new OnReadInterface() {
            @Override
            public void onRead(String message) {
                System.out.println(message);
            }
        });
        bluetoothManager.connectAsServer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == BluetoothManager.REQUEST_ENABLE_BT)
            if (!bluetoothManager.onActivityResult(resultCode)) {
                System.out.println("Bluetooth failed you nub");
                finish();
                System.exit(0);
            }

    }


}
