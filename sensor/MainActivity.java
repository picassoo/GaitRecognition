package com.example.muhammed.sensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;

public class MainActivity extends Activity {

    protected final static String URL_PREDICT = "http://ip:port";
    protected final static String URL_COLLECT = "http://ip:port/data";

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor gyroscope;

    Listener sensorListener;

    SampleSave accelerometerSampleSave;
    SampleSave gyroscopeSampleSave;
    SampleSaveBoth sampleSaveBoth;

    public TableLayout inflate;
    private RadioGroup radioSensorGroup, radioTimeGroup, radioDataGroup;
    private RadioButton radioTime3, radioTime4, radioTime5, radioTime6, radioAcc, radioGyro, acc, gro, both;
    private TextView infoText;
    public GraphView graph;
    public ViewGroup.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    private EditText nameEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = findViewById(R.id.info);

        radioSensorGroup = findViewById(R.id.sensor_select);
        radioTimeGroup = findViewById(R.id.time_select);
        radioDataGroup = findViewById(R.id.sensorData);

        nameEdit = findViewById(R.id.name);

        radioTime3 = findViewById(R.id.time_3);
        radioTime4 = findViewById(R.id.time_4);
        radioTime5 = findViewById(R.id.time_5);
        radioTime6 = findViewById(R.id.time_6);

        radioAcc = findViewById(R.id.accelerometer);
        radioGyro = findViewById(R.id.gyroscope);

        acc = findViewById(R.id.acc);
        gro = findViewById(R.id.gro);
        both = findViewById(R.id.both);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



        inflate = findViewById(R.id.mytable);

        inflate.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

    }


    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(accelerometerSampleSave, accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(accelerometerSampleSave);
    }

    public void clearResult(View view) {
        inflate.removeAllViews();
    }

    public void unregisterListener(View v) {
        unregisterListener();
        unregisterDataListener();
    }

    public void unregisterListener() {
        if (graph != null)
            graph.removeAllSeries();
        //graph = null;
        sensorManager.unregisterListener(sensorListener);
        sensorListener = null;

        infoText.setText("IDLE");
    }

    public void registerListener(View v) {
        unregisterListener();
        //unregisterDataListener();
        if (radioSensorGroup.getCheckedRadioButtonId() != -1) {
            int sensorId = radioSensorGroup.getCheckedRadioButtonId();
            if (radioTimeGroup.getCheckedRadioButtonId() != -1) {
                int timeId = radioTimeGroup.getCheckedRadioButtonId();
                int time;
                if (timeId == radioTime3.getId())
                    time = 3;
                else if (timeId == radioTime4.getId())
                    time = 4;
                else if (timeId == radioTime5.getId())
                    time = 5;
                else time = 6;

                if (sensorId == radioAcc.getId()) {
                    sensorListener = new Listener(this, Sensor.TYPE_ACCELEROMETER, time);
                    sensorManager.registerListener(sensorListener, accelerometer,
                        10000);

                    infoText.setText("TIME :: " + time + " \nSENSOR :: ACCELEROMETER");

                } else {
                    sensorListener = new Listener(this, Sensor.TYPE_GYROSCOPE, time);
                    sensorManager.registerListener(sensorListener, gyroscope,
                        10000);
                    infoText.setText("TIME :: " + time + " \nSENSOR :: GYROSCOPE");

                }
                Toast.makeText(this, "Listener is started", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(this, "Time is not selected.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sensor type is not selected.", Toast.LENGTH_SHORT).show();
        }

    }

    public void unregisterDataListener() {
        if (graph != null)
            graph.removeAllSeries();
        //graph = null;
        sensorManager.unregisterListener(accelerometerSampleSave);
        sensorManager.unregisterListener(gyroscopeSampleSave);
        sensorManager.unregisterListener(sampleSaveBoth);

        accelerometerSampleSave = null;
        gyroscopeSampleSave = null;
        sampleSaveBoth = null;
    }

    public void unregisterDataListener(View v) {
        if (accelerometerSampleSave != null) {
            accelerometerSampleSave.sendData();
        } else if (gyroscopeSampleSave != null) {
            gyroscopeSampleSave.sendData();
        } else if (sampleSaveBoth != null) {
            sampleSaveBoth.sendData();
        }
        unregisterListener();
        unregisterDataListener();
    }

    public void registerDataListener(View v) {
        unregisterListener();
        unregisterDataListener();
        if (nameEdit.getText().toString() != null || !nameEdit.getText().toString().isEmpty()) {
            String name = nameEdit.getText().toString();

            int selectedId = radioDataGroup.getCheckedRadioButtonId();
            if (selectedId == acc.getId()) {
                accelerometerSampleSave = new SampleSave(this, Sensor.TYPE_ACCELEROMETER, name);
                sensorManager.registerListener(accelerometerSampleSave, accelerometer,
                    10000);
            } else if (selectedId == gro.getId()) {
                Log.d("HEREM", "gyroscope is selected");
                gyroscopeSampleSave = new SampleSave(this, Sensor.TYPE_GYROSCOPE, name);
                sensorManager.registerListener(gyroscopeSampleSave, gyroscope,10000);
            } else if (selectedId == both.getId()) {
                sampleSaveBoth = new SampleSaveBoth(this, name);

            }
        }


    }

}
