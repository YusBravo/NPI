package com.pambudev.practica3_npi;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity{
    Accelerometer accelerometer;
    Float accelX, accelY, accelZ, prevX, prevY, prevZ;
    long lastUpdate, lastMov;
    final Handler myHandler = new Handler();
    int deltaTime = 30;

    protected void sensorThread() {
        Thread t = new Thread() {

            public void run() {
                while(true) {
                    try {
                        Thread.sleep(deltaTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    myHandler.post(ejecutarAccion);
                }
            }
        };
        t.start();
    }

    final Runnable ejecutarAccion = new Runnable(){
        public void run(){
            synchronized (this) {
                long currentTime = accelerometer.getAtTime();
                int limit = 1500;
                float minMov = 1E-6f;
                float mov, movX, movY, movZ;
                long timeDiff;

                accelX = accelerometer.getAccelX();
                accelY = accelerometer.getAccelY();
                accelZ = accelerometer.getAccelZ();

                ((TextView) findViewById(R.id.textAccX)).setText("Acelerómetro X: \n" + accelX);
                ((TextView) findViewById(R.id.textAccY)).setText("Acelerómetro Y: \n" + accelY);
                ((TextView) findViewById(R.id.textAccZ)).setText("Acelerómetro Z: \n" + accelZ);

                if (prevX == 0 && prevY == 0 && prevZ == 0) {
                    lastUpdate = currentTime;
                    lastMov = currentTime;
                    prevX = accelX;
                    prevY = accelY;
                    prevZ = accelZ;
                }

                timeDiff = currentTime - lastUpdate;

               // ((TextView) findViewById(R.id.time)).setText("\nTime: \n" + timeDiff+"\nlast mov:\n"+lastMov+"\nLast update:\n"+lastUpdate+"\nActual:\n"+currentTime+"\nresta:\n"+(currentTime - lastMov));

                if (timeDiff > 0) {
                    mov = Math.abs((accelX + accelY + accelZ) - (prevX - prevY - prevZ)) / timeDiff;
                    movX = (accelX - prevX) / timeDiff;
                    movY = (accelY - prevY) / timeDiff;
                    movZ = (accelZ - prevZ) / timeDiff;

                    if (mov > minMov) {
                        if (currentTime - lastMov >= limit) {
                            ((TextView) findViewById(R.id.movAccX)).setText("Mov in X: \n" + movX);
                            ((TextView) findViewById(R.id.movAccY)).setText("Mov in Y: \n" + movY);
                            ((TextView) findViewById(R.id.movAccZ)).setText("Mov in Z: \n" + movZ);
                        }

                        lastMov = currentTime;
                    }else{

                    }
                    ((TextView) findViewById(R.id.movAcc)).setText("Mov total: \n"+mov);

                    prevX = accelX;
                    prevY = accelY;
                    prevZ = accelZ;
                    lastUpdate = currentTime;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ = prevX = prevY = prevZ = 0f;

        sensorThread(); //Thread simultaneo para manejo del acelerometro (sensorEventListener)

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
