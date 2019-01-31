package com.example.muhammed.sensor;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TableRow;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class Listener implements SensorEventListener {

    private MainActivity mainActivity;
    private int time;
    float[] matrix = new float[3];
    int sensorType;
    private List<float[]> listInput;
    private int inputSize;
    private RequestQueue MyRequestQueue;
    private DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private LineGraphSeries<DataPoint> seriesX;
    private LineGraphSeries<DataPoint> seriesY;
    private LineGraphSeries<DataPoint> seriesZ;
    private LineGraphSeries<DataPoint> seriesAcc;
    private static double currentX;
    private static double currentY;
    private static double currentZ;
    private static double currentAcc;

    private ThreadPoolExecutor liveChartExecutor;
    private LinkedBlockingQueue<Double> accelerationQueueX = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    private LinkedBlockingQueue<Double> accelerationQueueY = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    private LinkedBlockingQueue<Double> accelerationQueueZ = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    private LinkedBlockingQueue<Double> accelerationQueueAcc = new LinkedBlockingQueue<>(Integer.MAX_VALUE);

    public Listener(MainActivity mainActivity, int sensorType, int time) {
        this.mainActivity = mainActivity;
        this.sensorType = sensorType;
        this.time = time;
        inputSize = time * 100;
        listInput = new ArrayList<>();
        MyRequestQueue = Volley.newRequestQueue(mainActivity);
        mainActivity.graph = mainActivity.findViewById(R.id.graph);

        seriesX = new LineGraphSeries<>();
        seriesX.setColor(Color.GREEN);
        mainActivity.graph.addSeries(seriesX);

        seriesY = new LineGraphSeries<>();
        seriesY.setColor(Color.BLUE);
        mainActivity.graph.addSeries(seriesY);

        seriesZ = new LineGraphSeries<>();
        seriesZ.setColor(Color.RED);
        mainActivity.graph.addSeries(seriesZ);

        seriesAcc = new LineGraphSeries<>();
        seriesAcc.setColor(Color.YELLOW);
        mainActivity.graph.addSeries(seriesAcc);

        mainActivity.graph.getViewport().setXAxisBoundsManual(true);
        mainActivity.graph.getViewport().setMinX(0.5D);
        mainActivity.graph.getViewport().setMaxX(40.D);
        mainActivity.graph.getViewport().setYAxisBoundsManual(true);
        mainActivity.graph.getViewport().setMinY(-30.0D);
        mainActivity.graph.getViewport().setMaxY(40.0D);

        currentX = 0;
        currentY = 0;
        currentZ = 0;
        currentAcc = 0;

        liveChartExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        if (liveChartExecutor != null)
            liveChartExecutor.execute(new AccelerationChart(new AccelerationChartHandler()));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (sensorType == event.sensor.getType()) {
            matrix = event.values.clone();

            getAccelerometer(matrix);
            mainActivity.graph.onDataChanged(true, false);
            runThread();
        }

    }

    private void getAccelerometer(float[] values) {

        double x = values[0];
        double y = values[1];
        double z = values[2];

        accelerationQueueX.offer(x);
        accelerationQueueY.offer(y);
        accelerationQueueZ.offer(z);
        mainActivity.graph.refreshDrawableState();

    }

    private class AccelerationChartHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Double accelerationX = 0.0D;
            Double accelerationY = 0.0D;
            Double accelerationZ = 0.0D;
            try {
                if (!msg.getData().getString("ACCELERATION_VALUE_X").equals(null) && !msg.getData().getString("ACCELERATION_VALUE_Y").equals("null") && !msg.getData().getString("ACCELERATION_VALUE_Z").equals("null")) {
                    accelerationY = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE_Y")));
                    accelerationX = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE_X")));
                    accelerationZ = (Double.parseDouble(msg.getData().getString("ACCELERATION_VALUE_Z")));
                }

                seriesX.appendData(new DataPoint(currentX, accelerationX), true, Integer.MAX_VALUE);
                seriesY.appendData(new DataPoint(currentY, accelerationY), true, Integer.MAX_VALUE);
                seriesZ.appendData(new DataPoint(currentZ, accelerationZ), true, Integer.MAX_VALUE);

                currentX = currentX + 0.1D;
                currentY = currentY + 0.1D;
                currentZ = currentZ + 0.1D;
            } catch (Exception e) {
            }
        }
    }

    private class AccelerationChart implements Runnable {
        private boolean drawChart = true;
        private Handler handler;

        public AccelerationChart(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            while (drawChart) {

                Double accelerationX;
                Double accelerationY;
                Double accelerationZ;
                Double acceleration;
                try {
                    Thread.sleep(1); // Speed up the X axis
                    accelerationX = accelerationQueueX.poll();
                    accelerationY = accelerationQueueY.poll();
                    accelerationZ = accelerationQueueZ.poll();
                    acceleration = accelerationQueueAcc.poll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (accelerationY == null)
                    continue;


                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();

                b.putString("ACCELERATION_VALUE_X", String.valueOf(accelerationX));
                b.putString("ACCELERATION_VALUE_Y", String.valueOf(accelerationY));
                b.putString("ACCELERATION_VALUE_Z", String.valueOf(accelerationZ));
                b.putString("ACCELERATION_VALUE_ACC", String.valueOf(acceleration));
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
        }
    }


    private void runThread() {

        new Thread() {
            public void run() {

                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (listInput.size() > inputSize) {
                            listInput = new ArrayList<>();
                            listInput.add(matrix);
                        } else if (listInput.size() < inputSize) {
                            listInput.add(matrix);
                        } else if (listInput.size() == inputSize) {
                            Log.d("ARRAY", "collected");
                            String url = "http://ip:port";

                            JSONObject jsonObject = new JSONObject();
                            try {
                                JSONArray jsonArray = new JSONArray(listInput);
                                jsonObject.put("data", jsonArray);
                                jsonObject.put("time", time);
                                if (sensorType == Sensor.TYPE_ACCELEROMETER)
                                    jsonObject.put("sensor", "acc");
                                else
                                    jsonObject.put("sensor", "gro");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.POST, mainActivity.URL_PREDICT, jsonObject, new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            String person = response.getString("walk");
                                            TableRow row = new TableRow(mainActivity);
                                            TextView txtcol1 = new TextView(mainActivity);
                                            txtcol1.setTextSize(20);
                                            txtcol1.setLayoutParams(mainActivity.params);
                                            txtcol1.setText(df.format(new Date()) + "---" + person);
                                            row.addView(txtcol1);
                                            mainActivity.inflate.addView(row);
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

                            Toast.makeText(mainActivity, "Sample collected", Toast.LENGTH_SHORT).show();
                            MyRequestQueue.add(jsonObjectRequest);
                            listInput = new ArrayList<>();
                            listInput.add(matrix);
                        }

                    }
                });

            }
        }.

            start();
    }

}