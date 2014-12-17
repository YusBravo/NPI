package com.pambudev.practica3_npi;

import android.content.Context;

/**
 * Created by Yusinho on 16/12/2014.
 */
public class Accelerometer implements AccelerometerInterface {
    AccelerometerHandler accelHandler;

    public Accelerometer(Context context){
        accelHandler = new AccelerometerHandler(context);
    }

    @Override
    public float getAccelX() {
        return accelHandler.getAccelX();
    }

    @Override
    public float getAccelY() {
        return accelHandler.getAccelY();
    }

    @Override
    public float getAccelZ() {
        return accelHandler.getAccelZ();
    }

    @Override
    public long getAtTime(){return accelHandler.getAtTime();}
}
