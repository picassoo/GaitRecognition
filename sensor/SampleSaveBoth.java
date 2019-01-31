package com.example.muhammed.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleSaveBoth implements SensorEventListener {

    private long lastUpdate = -1;
    private long curTime;

    private MainActivity mainActivity;
    private int time;
    private String name;
    float[] data = new float[6];
    private List<float[]> listInput;
    private RequestQueue MyRequestQueue;

    private static double currentX;
    private static double currentY;
    private static double currentZ;
    private static int NUMBER_OF_INPUT = 20000;


    public SampleSaveBoth(MainActivity mainActivity, String name) {
        this.mainActivity = mainActivity;
        this.name = name;
        Toast.makeText(mainActivity, "Collecting phase is started", Toast.LENGTH_SHORT).show();
        listInput = new ArrayList<>();
        MyRequestQueue = Volley.newRequestQueue(mainActivity);
        mainActivity.sensorManager.registerListener(this, mainActivity.accelerometer, 10000);
        mainActivity.sensorManager.registerListener(this, mainActivity.gyroscope,10000);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            data[0] = event.values.clone()[0];
            data[1] = event.values.clone()[1];
            data[2] = event.values.clone()[2];
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            data[3] = event.values.clone()[0];
            data[4] = event.values.clone()[1];
            data[5] = event.values.clone()[2];
        }
        curTime = event.timestamp; //in nanoseconds
        Log.d("Test Value" , Float.toString(curTime-lastUpdate));

        if ((curTime - lastUpdate) >= 10000000) {
            lastUpdate = curTime;
            runThread();

        }
    }


    public void sendData() {
        Log.d("ARRAY", "collected");
        Toast.makeText(mainActivity, "Data(acc+gyro) collected", Toast.LENGTH_SHORT).show();
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(listInput);
            jsonObject.put("data", jsonArray);
            jsonObject.put("time", time);
            jsonObject.put("name", name);
            jsonObject.put("sensor", "both");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.POST, mainActivity.URL_COLLECT, jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        int walk = response.getInt("walk");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error

                }
            }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        MyRequestQueue.add(jsonObjectRequest);

    }

    private void runThread() {

        new Thread() {
            public void run() {
                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (listInput.size() > NUMBER_OF_INPUT) {
                            listInput = new ArrayList<>();
                            listInput.add(data);
                        } else if (listInput.size() < NUMBER_OF_INPUT) {
                            listInput.add(data);
                        } else if (listInput.size() == NUMBER_OF_INPUT) {
                            sendData();
                            listInput = new ArrayList<>();
                            listInput.add(data);
                        }
                        data = new float[6];
                    }
                });

            }
        }.

            start();
    }
}
