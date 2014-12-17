package com.pambudev.practica3_npi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import java.util.List;

/**
 * Created by Yusinho on 16/12/2014.
 */
public class AccelerometerHandler implements SensorEventListener{
    float accelX;
    float accelY;
    float accelZ;
    long time;

    public AccelerometerHandler(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(context.SENSOR_SERVICE); //el manager que se va a usar para el listener

        if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            Sensor accelerometer = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_GAME);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No hacemos nada
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            accelX = event.values[0];
            accelY = event.values[1];
            accelZ = event.values[2];
            time = event.timestamp;
        }
    } //Guardamos los valores del acelerometro desde array event.values

    public float getAccelX() {
        return accelX;
    }

    public float getAccelY() {
        return accelY;
    }

    public float getAccelZ() {
        return accelZ;
    }

    public long getAtTime(){return time;}
}
