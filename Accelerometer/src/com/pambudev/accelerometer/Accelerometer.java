package com.pambudev.accelerometer;

import android.content.Context;

public class Accelerometer implements AccelerometerInterface{
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
	    public long getAtTime(){
	    	return accelHandler.getAtTime();
	    }
	    
	    @Override
	    public float getPower(){
	    	return accelHandler.getPower();
	    }
	    
	    
}
