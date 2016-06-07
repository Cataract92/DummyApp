package com.example.cataract.dummyapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by schleppi on 07.06.16.
 */
public class BluetoothManager {

    public final static int REQUEST_ENABLE_BT = 1337;

    private BluetoothAdapter adapter;
    private Activity activity ;

    private ReadWriteThread readWriteThread = null;
    private AcceptThread acceptThread = null;

    private OnReadInterface onReadInterface;

    public BluetoothManager(Activity activity, OnReadInterface onReadInterface){
        this.activity = activity;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.onReadInterface = onReadInterface;

        if (adapter == null) {
            System.out.println("Bluetooth not supported!");
            return;
        }
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }



    public boolean onActivityResult(int resultCode){
        if (resultCode == activity.RESULT_OK) return true;
        System.out.println("Bluetooth enable failed!");
        return false;
    }

    public void connectAsServer(){
        new AcceptThread().start();
    }

    public void write(String message){
        if (readWriteThread == null) return;
        readWriteThread.write(message.getBytes());
    }

    public void cancel() {
        try {
            if (readWriteThread != null) {
                readWriteThread.mmSocket.close();
                readWriteThread.mmInStream.close();
                readWriteThread.mmOutStream.close();
                readWriteThread = null;
                acceptThread = null;
            } else {
                if (acceptThread != null) {
                    acceptThread.mmServerSocket.close();
                    acceptThread = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private final UUID uuid = UUID.fromString("9085495d-f273-443d-9e37-e27c9194f63b");

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord("DummyApp", uuid);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    System.out.println("Waiting for connection!");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    cancel();
                    break;
                }
                if (socket != null) {
                    new ReadWriteThread(socket).start();
                }
            }
        }
    }

    private class ReadWriteThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ReadWriteThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut;
            tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];

            while (true) {
                try {
                    mmInStream.read(buffer);
                    onReadInterface.onRead(new String(buffer).trim());
                    break;
                } catch (IOException e) {
                    cancel();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
    }

}