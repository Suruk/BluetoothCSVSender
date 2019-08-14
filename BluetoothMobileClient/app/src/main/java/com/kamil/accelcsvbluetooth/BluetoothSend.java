package com.kamil.accelcsvbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class BluetoothSend extends AppCompatActivity {

    private static final String appUUID = "686ced60-a349-11e9-b475-0800200c9a66";
    private static final String TERMINATE_CONNECTION = "Ending Connection";

    private ConnectThread connectThread;
    private ConnectedThread connectedDoneThread;
    private BluetoothAdapter BA;
    private TextView deviceName;
    private ArrayAdapter<String> newDeviceAdapter;

    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_send);
        deviceName = findViewById(R.id.textView5);

        Bundle bundle = getIntent().getExtras();

        if(bundle == null){
            data = null;
        } else {
            data = bundle.getString("content");
        }

        newDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ArrayAdapter<String> bondedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        BA = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        if(!BA.isEnabled()){
            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 0);
        }

        ListView lister = findViewById(R.id.listView2);
        ListView bondage = findViewById(R.id.listView);
        lister.setAdapter(newDeviceAdapter);
        bondage.setAdapter(bondedDeviceAdapter);
        lister.setOnItemClickListener(listener);
        bondage.setOnItemClickListener(listener);

        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);


        //Write bonded devices to ArrayAdapter
        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bondedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    public void enable(View view){

        if (BA.isDiscovering()) {
            BA.cancelDiscovery();
        }
        BA.startDiscovery();


    }

    public void disconnect(View view){
        stop();
    }

    //Method used by Threads to change name of device currently connected to
    private void setText (final String name){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceName.setText(name);
            }
        });

    }


    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            BA.cancelDiscovery();

            //Getting MAC address (xx-yy-zz-aa-bb-cc) and connecting to listed device
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            pairDevice(address);


        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                newDeviceAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(BluetoothSend.this, "Discovery finished", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private synchronized void pairDevice(String address){

        stop();

        BluetoothDevice device = BA.getRemoteDevice(address);
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device){

        stop();

        connectedDoneThread = new ConnectedThread(socket);
        connectedDoneThread.start();
        String name = device.getName();
        setText(name);
    }

    private synchronized void stop(){

        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }

        if(connectedDoneThread != null){
            connectedDoneThread.cancel();
            connectedDoneThread = null;
        }
        setText("");
    }

    //Thread responsible for creating Bluetooth connection with server
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            UUID uuid = UUID.fromString(appUUID);
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            BA.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return;
            }

            synchronized (BluetoothSend.this) {
                connectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        private void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Thread responsible for sending data
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        private ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmOutStream = tmpOut;
        }

        public void run() {
            byte [] content = data.getBytes();
            write(content);
        }

        private void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cancel() {
            try {
                byte [] buffer = TERMINATE_CONNECTION.getBytes();
                write(buffer);
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
