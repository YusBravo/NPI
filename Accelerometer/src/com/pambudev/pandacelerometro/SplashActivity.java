/**
 * T�tulo: Aceler�metro
 * Licencia P�blica General de GNU (GPL) versi�n 3 
 * Autores:
 * - Jos� Francisco Bravo S�nchez (Yus Bravo)
 * - Pedro Fern�ndez Bosch
 * Fecha de la �ltima modificaci�n: 08/01/2015
 */

package com.pambudev.pandacelerometro;

import java.util.Timer;
import java.util.TimerTask;

import com.pambudev.accelerometer.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;

public class SplashActivity extends Activity {

	/**
	 * Declaraci�n de variables 
	 */
    int deltaTime = 3000;

    /**
	 * OnCreate Method Override 
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
 
                // Start the next activity
                Intent mainIntent = new Intent().setClass(SplashActivity.this, WelcomeScreenActivity.class);
                startActivity(mainIntent);
 
                // Close the activity so the user won't able to go back this
                // activity pressing Back button
                finish();
            }
        };
 
        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, deltaTime);
    }

	
    @Override
    protected void onResume() {
        super.onResume();
       
    }

}
