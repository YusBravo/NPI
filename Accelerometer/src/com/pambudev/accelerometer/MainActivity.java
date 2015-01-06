package com.pambudev.accelerometer;


import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Accelerometer accelerometer;
    Float accelX, accelY, accelZ, prevX, prevY, prevZ;
    long lastUpdate, lastMov;
    int sdk = android.os.Build.VERSION.SDK_INT;
    final Handler myHandler = new Handler();
    int deltaTime = 40;
    private Button stateButton,
    			   arribaButton,
    			   abajoButton,
    			   derechaButton,
    			   izquierdaButton,
    			   arribaProfundoButton,
    			   abajoProfundoButton;
    private ImageView pambil;
    private int stateApp = 1;
    int cont = 0;
    public enum TipoMovimiento{
    	NINGUNO, ARRIBA, ABAJO, DERECHA, IZQUIERDA, ARRIBAPROFUNDO,ABAJOPROFUNDO
    }
    
    private TipoMovimiento tipoMov = TipoMovimiento.NINGUNO;

    protected void sensorThread() {
        Thread t = new Thread() {

            public void run() {
                while(true) {
                    try {
                        Thread.sleep(deltaTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(stateApp == 1)
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
                int limit = 40000000; //0,04 segundos
                float minMov = 1E-6f;
                float minMovSide = 1E-9f;
                float mov, movX, movY, movZ;
                long timeDiff;

                accelX = accelerometer.getAccelX();
                accelY = accelerometer.getAccelY();
                accelZ = accelerometer.getAccelZ();

                ((TextView) findViewById(R.id.valueX)).setText(accelX.toString());
                ((TextView) findViewById(R.id.valueY)).setText(accelY.toString());
                ((TextView) findViewById(R.id.valueZ)).setText(accelZ.toString());

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
                 
                    if (currentTime - lastMov >= limit) {
                    	 mov = Math.abs((accelX + accelY + accelZ) - (prevX - prevY - prevZ)) / timeDiff;
                         movX = (accelX - prevX) / timeDiff;
                         movY = (accelY - prevY) / timeDiff;
                         movZ = (accelZ - prevZ) / timeDiff;
                         
                    	if (mov > minMov) {
                            ((TextView) findViewById(R.id.valueMovX)).setText(""+movX);
                            ((TextView) findViewById(R.id.valueMovY)).setText(""+movY);
                            ((TextView) findViewById(R.id.valueMovZ)).setText("" + movZ);
                            
                            switch(tipoMov){
	                            case DERECHA:
	                            	if(movX > minMovSide)
	                        			cont++;
	                            	break;
	                            case IZQUIERDA:
	                        		if(movX < -minMovSide)
	                        			cont++;
	                            	break;
	                            case ARRIBA:
	                            	if(movY > minMovSide)
	                        			cont++;
	                            	break;
	                            case ABAJO:
	                            	if(movY < -minMovSide)
	                        			cont++;
	                            	break;
	                            case ARRIBAPROFUNDO:
	                            	if(movZ < -minMovSide)
	                        			cont++;
	                            	break;
	                            case ABAJOPROFUNDO:
	                            	if(movZ > minMovSide)
	                        			cont++;
	                            	break;
	                            default:
	                            	break;
                            }
                        }

                        lastMov = currentTime;
                    }else{

                    }
                    
                    ((TextView) findViewById(R.id.sensorPowerValue)).setText("" + accelerometer.getPower());
                    
                	TextView contador = (TextView)findViewById(R.id.contador);        	       
        	        contador.setText(String.valueOf(cont));
                    
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
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ = prevX = prevY = prevZ = 0f;

        pambil = (ImageView) findViewById(R.id.pambil);
        
        setStateButton();
        setArribaButton();
        setAbajoButton();
        setDerechaButton();
        setIzquierdaButton();
        setArribaProfundoButton();
        setAbajoProfundoButton();
        
        sensorThread(); //Thread simultaneo para manejo del acelerometro (sensorEventListener)

    }

    
    /**
	 * Sets up the listener for the state button that the user
	 * must click to change state of the app (running or pause) 
	 */
	private void setStateButton() {
		// Reference the speak button
		stateButton = (Button) findViewById(R.id.stateButton);

		// Set up click listener
		stateButton.setOnClickListener(new OnClickListener() {              
	        @SuppressWarnings("deprecation")
			@Override 
	        public void onClick(View v) {  
	        	if(stateApp == 0){
	        		stateApp = 1;       		
	        	}else{
	        		stateApp = 0;
	        	} 	
	        	
	        	if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
	        	    
	        		if(stateApp == 1){
	        			stateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
	        			changePambilImage();
	        			changeFeedbackText();
	        		}else{
	        			stateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
	        			pambil.setImageResource(R.drawable.pambil_pause);
	        			((TextView) findViewById(R.id.feedbackText)).setText("ZzZzZz...");
	        		}
	        			
	        	} else {
	        		
	        		if(stateApp == 1){
	        			stateButton.setBackground(getResources().getDrawable(R.drawable.pause));
	        			changePambilImage();
	        			changeFeedbackText();
	        		}else{
	        			stateButton.setBackground(getResources().getDrawable(R.drawable.play));	
	        			pambil.setImageResource(R.drawable.pambil_pause);
	        			((TextView) findViewById(R.id.feedbackText)).setText("ZzZzZz...");
	        		}
	        	  
	        	}
	        	
	        }  
	    });

	}
	
	
	private void changePambilImage(){
		switch(tipoMov){
			case ARRIBA:
				pambil.setImageResource(R.drawable.pambil_arriba);
				break;
			case ABAJO:
				pambil.setImageResource(R.drawable.pambil_abajo);
				break;
			case IZQUIERDA:
				pambil.setImageResource(R.drawable.pambil_izquierda);
				break;
			case DERECHA:
				pambil.setImageResource(R.drawable.pambil_derecha);
				break;
			case ABAJOPROFUNDO:
				pambil.setImageResource(R.drawable.pambil_acercar);
				break;
			case ARRIBAPROFUNDO:
				pambil.setImageResource(R.drawable.pambil_alejar);
				break;
			default:
				pambil.setImageResource(R.drawable.pambil_play);
				break;			
		}
	}
	
	private void changeFeedbackText(){
				
		switch(tipoMov){
			case ARRIBA:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia arriba");
				break;
			case ABAJO:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia abajo");
				break;
			case IZQUIERDA:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia la izquierda");
				break;
			case DERECHA:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia la derecha");
				break;
			case ABAJOPROFUNDO:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia el fondo");
				break;
			case ARRIBAPROFUNDO:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia ti");
				break;
			default:
				pambil.setImageResource(R.drawable.pambil_play);
				((TextView) findViewById(R.id.feedbackText)).setText("¡Bienvenido! Elije una opción");
				break;			
		}
	}
	
	 /**
	 * Sets up the listener for the arriba button that the user
	 * must click to change tipoMov to arriba 
	 */
	private void setArribaButton() {
		// Reference the speak button
		arribaButton = (Button) findViewById(R.id.arriba);

		// Set up click listener
		arribaButton.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.ARRIBA;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a arriba", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	 /**
	 * Sets up the listener for the abajo button that the user
	 * must click to change tipoMov to abajo 
	 */
	private void setAbajoButton() {
		// Reference the speak button
		abajoButton = (Button) findViewById(R.id.abajo);

		// Set up click listener
		abajoButton.setOnClickListener(new OnClickListener() {              
	       			
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.ABAJO;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a abajo", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	 /**
	 * Sets up the listener for the derecha button that the user
	 * must click to change tipoMov to derecha 
	 */
	private void setDerechaButton() {
		// Reference the speak button
		derechaButton = (Button) findViewById(R.id.derecha);

		// Set up click listener
		derechaButton.setOnClickListener(new OnClickListener() {              
	       
			
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.DERECHA;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a derecha", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	/**
	 * Sets up the listener for the izquierda button that the user
	 * must click to change tipoMov to izquierda 
	 */
	private void setIzquierdaButton() {
		// Reference the speak button
		izquierdaButton = (Button) findViewById(R.id.izquierda);

		// Set up click listener
		izquierdaButton.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.IZQUIERDA;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a izquierda", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	/**
	 * Sets up the listener for the arribaProfundo button that the user
	 * must click to change tipoMov to arribaProfundo 
	 */
	private void setArribaProfundoButton() {
		// Reference the speak button
		arribaProfundoButton = (Button) findViewById(R.id.arriba_profundo);

		// Set up click listener
		arribaProfundoButton.setOnClickListener(new OnClickListener() {              
	       
			
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.ARRIBAPROFUNDO;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a alejar", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	/**
	 * Sets up the listener for the abajo profundo button that the user
	 * must click to change tipoMov to abajo profundo 
	 */
	private void setAbajoProfundoButton() {
		// Reference the speak button
		abajoProfundoButton = (Button) findViewById(R.id.abajo_profundo);

		// Set up click listener
		abajoProfundoButton.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				tipoMov = TipoMovimiento.ABAJOPROFUNDO;
				cont = 0;
				changePambilImage();
				changeFeedbackText();
				Toast toast = Toast.makeText(getApplicationContext(),"Cambiado movimiento a acercar", Toast.LENGTH_SHORT);
				toast.show();
	        }  
	    });

	}
	
	

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}
*/
    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
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
    }*/
}
