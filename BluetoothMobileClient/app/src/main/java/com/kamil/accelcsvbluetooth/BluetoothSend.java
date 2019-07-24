package com.kamil.accelcsvbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothSend extends AppCompatActivity {

    private BluetoothAdapter BA;
    private ListView lister;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket socket;
    private BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_send);

        BA = BluetoothAdapter.getDefaultAdapter();
        lister = (ListView)findViewById(R.id.listview);
        lister.setClickable(true);
        lister.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for(BluetoothDevice bt : pairedDevices){
                    if (bt.getName().equals((String)lister.getItemAtPosition(i))){
                        device = bt;
                    }
                }
                //socket = device.createRfcommSocketToServiceRecord(MY_)

            }
        });
    }

    public void enable(View view){
        if(!BA.isEnabled()){
            Intent on = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(on, 0);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 0:
                if(resultCode == RESULT_OK){
                    pairedDevices = BA.getBondedDevices();

                    ArrayList list = new ArrayList();

                    for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());
                    final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
                    lister.setAdapter(adapter);
                }
        }

        super.onActivityResult(requestCode,resultCode,data);
    }

}
