package com.example.amazfit.falldetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class PostureDetection extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    public double ax, ay, az;
    public double normalvector_size;
    public double v_sum;
    public static String current_state;
    public static String previous_state;
    public int i = 0, flag = 0;
    double threshold = 0.5;
    double threshold10 = 10;
    double threshold5 = 5;
    double threshold2 = 2;
    static int BUFFER_SIZE = 100;
    static public double[] window = new double[BUFFER_SIZE];

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture_recognition);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        initialize();
    }

    private void initialize() {
        for (i = 0; i < BUFFER_SIZE; i++) {
            window[i] = 0;
        }
        previous_state = "none";
        current_state = "none";
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @SuppressLint("ParserError")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            v_sum = Math.sqrt((ax * ax) + (ay * ay) + (az * az));
            FillBuffer();
            posture_recognition(window, ay);
            StateChanger(current_state, previous_state);
        }
    }

    private void posture_recognition(double[] window2, double ay2) {
        int computeZrc = computeZrc(window2);
        if (computeZrc == 0) {

            if (Math.abs(ay2) < threshold5) {
                current_state = "sitting";
            } else {
                current_state = "standing";
            }
            flag = 0;

        } else {

            if (computeZrc > threshold2) {
                current_state = "walking";
                flag = 0;
            } else {

                current_state = "none";
                if (v_sum < 3) {
                    flag = 1;
                }
                if (v_sum > 17 && flag == 1) {
                    flag = 2;
                }
                if (v_sum > 3 && v_sum < 15 && flag == 2) {

                    current_state = "fall";
                }
            }
        }
    }

    private int computeZrc(double[] window2) {
        int count = 0;
        for (i = 1; i <= BUFFER_SIZE - 1; i++) {

            if ((window2[i] - threshold10) < threshold && (window2[i - 1] - threshold10) > threshold) {
                count = count + 1;
            }
        }
        return count;
    }

    private void StateChanger(String curr_state1, String prev_state1) {

        //állapot változás esetén
        if (!prev_state1.equalsIgnoreCase(curr_state1)) {
            if (curr_state1.equalsIgnoreCase("fall")) {
                Toast.makeText(PostureDetection.this, "FALLING", Toast.LENGTH_SHORT).show();
            }
            if (curr_state1.equalsIgnoreCase("sitting")) {
                Toast.makeText(PostureDetection.this, "SITTING", Toast.LENGTH_SHORT).show();
            }
            if (curr_state1.equalsIgnoreCase("standing")) {
                Toast.makeText(PostureDetection.this, "STANDING", Toast.LENGTH_SHORT).show();
            }
            if (curr_state1.equalsIgnoreCase("walking")) {
                Toast.makeText(PostureDetection.this, "WALKIING", Toast.LENGTH_SHORT).show();
            }
        }

        if (!previous_state.equalsIgnoreCase(current_state)) {
            previous_state = current_state;
        }
    }

    //következő érték betöltése bufferba
    private void FillBuffer() {
        for (i = 0; i <= BUFFER_SIZE - 2; i++) {
            window[i] = window[i + 1];
        }
        normalvector_size = Math.sqrt(ax * ax + ay * ay + az * az);
        window[BUFFER_SIZE - 1] = normalvector_size;
    }

    public void exit_app(View view) {
        finish();
    }
}
