/**
 * Título: Acelerómetro
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autores:
 * - José Francisco Bravo Sánchez (Yus Bravo)
 * - Pedro Fernández Bosch
 * Fecha de la última modificación: 03/02/2015
 */

package com.pambudev.pandacelerometro;

import com.pambudev.accelerometer.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.*;


public class MainActivity extends Activity {

	/**
	 * Declaración de variables 
	 */
	Accelerometer accelerometer;
    Float accelX, accelY, accelZ;
    long lastUpdate, lastMov;
    int sdk = android.os.Build.VERSION.SDK_INT;
    final Handler myHandler = new Handler();
    int deltaTime = 40;
    boolean accelerometerInitiated;
    private Button stateButton,
    			   arribaButton,
    			   abajoButton,
    			   derechaButton,
    			   izquierdaButton,
    			   arribaProfundoButton,
    			   abajoProfundoButton,
    			   iconMenuButton;
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

    /**
	 * Función principal Acelerómetro
	 * Valores ejes X, Y, Z
	 * Valores de aceleración 
	 */
    final Runnable ejecutarAccion = new Runnable(){
        public void run(){
            synchronized (this) {
                long currentTime = accelerometer.getAtTime();
                int limit = 35000000; //0,035 segundos
                float minMov = 1E-6f;
                float mov, movX, movY, movZ;
                long timeDiff;

                accelX = accelerometer.getAccelX();
                accelY = accelerometer.getAccelY();
                accelZ = accelerometer.getAccelZ();

                ((TextView) findViewById(R.id.valueX)).setText(accelX.toString());
                ((TextView) findViewById(R.id.valueY)).setText(accelY.toString());
                ((TextView) findViewById(R.id.valueZ)).setText(accelZ.toString());

                if (!accelerometerInitiated) {
                    lastUpdate = currentTime;
                    lastMov = currentTime;
                    accelerometer.actPrevAxisValues();
                    accelerometerInitiated = true;
                }

                timeDiff = currentTime - lastUpdate;
               
                if (timeDiff > 0) {
                	
                    if (currentTime - lastMov >= limit) { //intervalo en el cual realizar las comprobaciones
                    	
                    	accelerometer.actAxisMov(timeDiff);
                    	mov = accelerometer.getTotalMov();
                    	movX = accelerometer.getMovXValue();
                    	movY = accelerometer.getMovYValue();
                    	movZ = accelerometer.getMovZValue();
                    	 
                    	if (mov > minMov) {
                            ((TextView) findViewById(R.id.valueMovX)).setText(""+movX);
                            ((TextView) findViewById(R.id.valueMovY)).setText(""+movY);
                            ((TextView) findViewById(R.id.valueMovZ)).setText("" + movZ);
                            
                            switch(tipoMov){
	                            case DERECHA:
	                            	if(accelerometer.isPositiveMovX())
	                        			cont++;
	                            	break;
	                            case IZQUIERDA:
	                        		if(accelerometer.isNegativeMovX())
	                        			cont++;
	                            	break;
	                            case ARRIBA:
	                            	if(accelerometer.isPositiveMovY())
	                        			cont++;
	                            	break;
	                            case ABAJO:
	                            	if(accelerometer.isNegativeMovY())
	                        			cont++;
	                            	break;
	                            case ABAJOPROFUNDO:
	                            	if(accelerometer.isPositiveMovZ())
	                        			cont++;
	                            	break;
	                            case ARRIBAPROFUNDO:
	                            	if(accelerometer.isNegativeMovZ())
	                        			cont++;
	                            	break;
	                            default:
	                            	break;
                            }
                        }

                        lastMov = currentTime;
                    }
                    
                    ((TextView) findViewById(R.id.sensorPowerValue)).setText("" + accelerometer.getPower());
                    
                	TextView contador = (TextView)findViewById(R.id.contador);        	       
        	        contador.setText(String.valueOf(cont));
                    
        	        accelerometer.actPrevAxisValues();
                    lastUpdate = currentTime;
                }
            }
        }
    };

    /**
	 * OnCreate Method Override 
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        ((TextView) findViewById(R.id.txtMenu)).setText("Test Mode");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ =  0f;
        accelerometerInitiated = false;

        pambil = (ImageView) findViewById(R.id.pambil);
        
        setStateButton();
        setArribaButton();
        setAbajoButton();
        setDerechaButton();
        setIzquierdaButton();
        setArribaProfundoButton();
        setAbajoProfundoButton();     
        setIconMenuButton();    
        
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
	
	/**
	 * Change Pambil image foreach tipoMov
	 */
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
	
	/**
	 * Change feedback text foreach tipoMov
	 */
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
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia ti");
				break;
			case ARRIBAPROFUNDO:
				((TextView) findViewById(R.id.feedbackText)).setText("Realiza movimientos hacia el fondo");
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

	 /**
		 * Sets up the listener for menu for the iconMenu button
		 */
	    private void setIconMenuButton(){
	    	iconMenuButton = (Button) findViewById(R.id.iconMenu);
	    	
	    	// Set up click listener
				iconMenuButton.setOnClickListener(new OnClickListener() {              
			       
					@Override 
			        public void onClick(View v) {  
						 if(v.getId()==findViewById(R.id.iconMenu).getId()){
							 openOptionsMenu();
						 }
						
			        }  
		    });
	    }
		 
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {

	    	super.onCreateOptionsMenu(menu);
	    	
	    	MenuInflater inflater = getMenuInflater();
	 	    inflater.inflate(R.menu.context_menu, menu);
	 	
	        return true;
	    }
		
	    @Override
	    protected void onResume() {
	        super.onResume();
	    }

	  
	    /**
	     * item selected for Menu
	     */
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	boolean res = false;   	
	    	Toast toast;
	    	
	        switch (item.getItemId()) {
	        	case R.id.ResumeOption:
	        		res = true;
	        		break;
	            case R.id.CreditOption:
	            	toast = Toast.makeText(getApplicationContext(),
	            			"Desarrollado por:\n\n" +
	            			"- Yus Bravo (Pambú! Developers.)\n" +
	            			"- Pedro Bosch (www.pedrobosch.es)\n" +
	            			"\n" +
	            			"Los derechos de la aplicación pertenecen a Pambú! Dev.", Toast.LENGTH_SHORT);
	            	toast.setDuration(Toast.LENGTH_LONG);
	            	toast.show();
	                res = true;
	                break;
	            case R.id.GamesOption:
	            	
	            	try {
	            	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Pamb%C3%BA!+Dev.")));
	            	} catch (android.content.ActivityNotFoundException anfe) {
	            	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Pamb%C3%BA!+Dev.")));
	            	}
	            	           	
	                res= true;
	                break;
	            case R.id.SourceCodeOptionYus:
	            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YusBravo/NPI/tree/master/Accelerometer")));
	                res= true;
	                break;
	            case R.id.SourceCodeOptionPedro:
	            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pebosch/npi")));
	                res= true;
	                break;
	            case R.id.ExitOption:
	            	 finish();
	            	break;
	            default:
	                res= super.onContextItemSelected(item);
	                break;
	        }
	        
	        return res;		
	    }
    
}
