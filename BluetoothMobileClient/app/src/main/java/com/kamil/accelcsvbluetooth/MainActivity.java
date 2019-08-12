package com.kamil.accelcsvbluetooth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    Spinner s1;
    Spinner s2;
    TextView results;

    private SensorManager manager;
    private Sensor accel;
    private double ax,ay,az;

    private static final int PERMISSION_GRANT = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        results = findViewById(R.id.results);
        s1 = findViewById(R.id.spinner);
        s2 = findViewById(R.id.spinner2);

        ArrayAdapter a1 = ArrayAdapter.createFromResource(this, R.array.times, R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter a2 = ArrayAdapter.createFromResource(this, R.array.ticks, R.layout.support_simple_spinner_dropdown_item);

        a1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        a2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        s1.setAdapter(a1);
        s2.setAdapter(a2);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert manager != null;
        accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this,accel,Sensor.TYPE_ACCELEROMETER);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        ax = event.values[0];
        ay = event.values[1];
        az = event.values[2];
    }

    public void starter(View view) {

        PackageManager pmgr = getPackageManager();

        Boolean sensorCheck;
        sensorCheck = pmgr.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        if (sensorCheck) {

            Toast msg = Toast.makeText(this,"Sensors ready, starting measure", Toast.LENGTH_SHORT);
            msg.show();

            int time = Integer.parseInt(s1.getSelectedItem().toString());
            int tick = Integer.parseInt(s2.getSelectedItem().toString());

            final Toast comp = Toast.makeText(this,"Operation Complete", Toast.LENGTH_SHORT);

            new CountDownTimer(time * 60 * 1000, tick * 1000) {


                @Override
                public void onTick(long l) {
                String stamp = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());

                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_UP);

                String asx = df.format(ax);
                String asy = df.format(ay);
                String asz = df.format(az);


                String point = stamp+";"+asx+";"+asy+";"+asz+System.lineSeparator();
                results.append(point);

                }

                @Override
                public void onFinish() {

                    comp.show();
                }
            }.start();
        }

        else {
            Toast msg = Toast.makeText(this,"Sensors not available", Toast.LENGTH_SHORT);
            msg.show();
        }
    }

    public void txtFile (View view){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Saved_Results");
            if (!root.exists()) {
                root.mkdirs();
            }
            boolean dirextists = root.exists();
            if(dirextists){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_GRANT);
                }
                File file = new File(root, "Result");
                FileWriter writer = new FileWriter(file);
                writer.append(results.getText());
                writer.flush();
                writer.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Directory creation failed", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void startBluetooth (View view){
        Intent intent = new Intent(this, BluetoothSend.class);
        String data = results.getText().toString();
        intent.putExtra("content",data);
        startActivity(intent);
    }

}