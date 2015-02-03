/**
 * Título: Acelerómetro
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * - José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 03/02/2015
 */

package com.pambudev.pandacelerometro;

import java.util.Random;

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
import android.graphics.Color;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class OnePlayerActivity extends Activity {

	/**
	 * Declaración de variables 
	 */
	
	private static final int ARRIBA = 0;
	private static final int ABAJO = 1;
	private static final int ACERCAR = 2;
	private static final int ALEJAR = 3;
	private static final int DERECHA = 4;
	private static final int IZQUIERDA = 5;
	
	Accelerometer accelerometer;
	Random random = new Random();	
    Float accelX, accelY, accelZ;
    long lastUpdate, lastMov, lastPlayerWait;
    int sdk = android.os.Build.VERSION.SDK_INT;
    final Handler myHandler = new Handler();
    int deltaTime = 40;
    boolean accelerometerInitiated;
    private Button stateButton,    			  
    			   iconMenuButton;
    private ImageView pambil,timeNumImage;
    private int stateApp = 0;
    int cont = 0;
    public enum TipoMovimiento{
    	NINGUNO, ARRIBA, ABAJO, DERECHA, IZQUIERDA, ARRIBAPROFUNDO,ABAJOPROFUNDO,SLEEP
    }
    
    private TipoMovimiento tipoMov = TipoMovimiento.NINGUNO;
    
    int[] secuenciaPambilDice;
    
    public enum Turno{
    	NADIE, PAMBIL, JUGADOR,ESPERAJUGADOR, DERROTA
    };
    
    Turno turno = Turno.NADIE;
    Turno prevTurno = Turno.NADIE;
    long lastGameUpdate;
    int timePambilDice = 2000;
    int timePambilWait = 500;
    int timePlayerReaction = 3000;
    int timeGameWait = 1500;
    int timePlayerWait = 3000;
    int timeImage = 3;
    int actualMovDice = 0;
    int actualMaxMovDice = 3;
    boolean estaDiciendo = false;
    boolean jugadorDiceBien = true;
    boolean esperaJugador = false;
    
    int actualErrors = 0;
    int maxErrors = 6;
    TextView turnoText;
    

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

                if (!accelerometerInitiated) {
                    lastUpdate = currentTime;
                    lastMov = currentTime;
                    accelerometer.actPrevAxisValues();
                    accelerometerInitiated = true;
                }

                timeDiff = currentTime - lastUpdate;
               
                if (timeDiff > 0 && !esperaJugador) {
                	
                    if (currentTime - lastMov >= limit) { //intervalo en el cual realizar las comprobaciones
                    	
                    	accelerometer.actAxisMov(timeDiff);
                    	mov = accelerometer.getTotalMov();
                    	movX = accelerometer.getMovXValue();
                    	movY = accelerometer.getMovYValue();
                    	movZ = accelerometer.getMovZValue();
                    	 
                    	//if (mov > minMov) {
                            
                    		if(movimientoBienHecho()){
            					actualMovDice++;
            					actualErrors = 0;
            				
        	    				if(actualMovDice == (actualMaxMovDice-1)){
        	    					prevTurno = Turno.JUGADOR;
        	    					turno = Turno.NADIE;
        	    					turnoText.setText("¡Bien hecho!");
        	    					turnoText.setTextColor(Color.parseColor("#00ff00"));
        	    					turnoText.setVisibility(View.VISIBLE);
        	    					timeGameWait = 2000;
        	    					actualMovDice=0;
        	    				}else{
        	    					esperaJugador= true;
        	    					timeNumImage.setVisibility(View.INVISIBLE);
        	    					lastPlayerWait = System.currentTimeMillis();
        	    				
        	    				}
            				}else{
            					//if(actualErrors == maxErrors)
            					//	jugadorDiceBien = false;
            				}
                      //  }

                        lastMov = currentTime;
                    }
                  
        	        accelerometer.actPrevAxisValues();
                    lastUpdate = currentTime;
                }
            }
            juegoPambilDice();
        }
    };
    
    public void juegoPambilDice(){
    	if(stateApp == 1){
    		
    		long currentTime = System.currentTimeMillis();
    		
    		if(lastGameUpdate == 0){
    			lastGameUpdate = currentTime;
    		}
    		
    		changeFeedbackText();
    		
    		switch(turno){
	    		case PAMBIL:
	    			if(estaDiciendo){
	    				mostrarActualPambilDice();
	    				
		    			if(currentTime - lastGameUpdate > timePambilDice){
		    				lastGameUpdate = currentTime;
		    				
		    				if(actualMovDice == (actualMaxMovDice-1)){
		    					prevTurno = Turno.PAMBIL;
		    					turno = Turno.NADIE;
		    					timeGameWait = 1500;
		    					tipoMov = TipoMovimiento.NINGUNO;
		    					changePambilImage();
		    					actualMovDice=0;
		    					actualMaxMovDice++;
		    					turnoText.setVisibility(View.INVISIBLE);
		    				}else{
		    					
		    					actualMovDice++;
		    				}
		    				
		    				estaDiciendo = false;
		    			}
	    			}else{
	    				tipoMov = TipoMovimiento.SLEEP;
	    				changePambilImage();
	    				
	    				if(currentTime - lastGameUpdate > timePambilWait){
		    				lastGameUpdate = currentTime;
		    				estaDiciendo=true;
	    				}
	    				
	    				
	    			}
	    			
	    			break;
	    		case JUGADOR:
	    			if(!esperaJugador){
		    			if(currentTime - lastGameUpdate > 1000){
		    				lastGameUpdate = currentTime;
		    				timeImage--;
		    				
		    				if(timeImage == 0){
		    					jugadorDiceBien = false;
		    				}
		    				
		    				actualizarTimeImage();
		    				
		    				if(!jugadorDiceBien){
		    					prevTurno = Turno.JUGADOR;
			    				turno = Turno.NADIE;
		    					turnoText.setVisibility(View.INVISIBLE);
			    			}
		    			}
	    			}else{	
	                	if((currentTime - lastGameUpdate) > 2000){
	                		esperaJugador = false;
	                		tipoMov = TipoMovimiento.SLEEP;
	    					changePambilImage();
	    					timeNumImage.setVisibility(View.VISIBLE);
	    					timeImage = 4;
	                	}
		                
		    		}
		    			
	    			  			
	    			break;
	    		case ESPERAJUGADOR:
	    			if(currentTime - lastPlayerWait > timePlayerWait){
	    				turno = Turno.JUGADOR;
	    				lastGameUpdate = currentTime;
	    				tipoMov = TipoMovimiento.SLEEP;
    					changePambilImage();
    					timeNumImage.setVisibility(View.VISIBLE);
	    			}
	    			break;
	    		case NADIE:
	    			if(currentTime - lastGameUpdate > timeGameWait){
	    				lastGameUpdate = currentTime;
	    				actualMovDice=0;
	    				actualErrors = 0;
	    				
	    				if(prevTurno == Turno.PAMBIL){
	    					esperaJugador = false;
	    					turno = Turno.JUGADOR;
	    					timeNumImage.setVisibility(View.VISIBLE);
	    					
	    				}else if(prevTurno == Turno.JUGADOR){
	    					
	    					timeNumImage.setVisibility(View.INVISIBLE);
	    					if(jugadorDiceBien && actualMaxMovDice < 50){
		    					turno = Turno.PAMBIL;	
		    					
	    					}else{
	    						turno = Turno.DERROTA;
	    					}
	    				}
	    			}
	    			break;
	    		case DERROTA:
	    			if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {		        	     		
		        			stateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.restart));	        			
		        	} else {
		        			stateButton.setBackground(getResources().getDrawable(R.drawable.restart));	
		        			  
		        	}
	    			tipoMov = TipoMovimiento.NINGUNO;
    				changePambilImage();
	    			break;
    		}
    		
    	}
    	
    }
    
    public boolean movimientoBienHecho(){
    	boolean res = false;
    	
    	float minMov = 1E-7f;
    	
    	switch(secuenciaPambilDice[actualMovDice]){
    	 case DERECHA:
         	if(accelerometer.isPositiveMovX()  && Math.abs(accelerometer.getMovXValue()) > minMov
         									   && Math.abs(accelerometer.getMovXValue())> (Math.abs(accelerometer.getMovZValue()))
         									   && Math.abs(accelerometer.getMovXValue())> (Math.abs(accelerometer.getMovYValue())))
     			res = true;
         	break;
         case IZQUIERDA:
     		if(accelerometer.isNegativeMovX()  && Math.abs(accelerometer.getMovXValue())> (Math.abs(accelerometer.getMovZValue()))
     										   && Math.abs(accelerometer.getMovXValue())> (Math.abs(accelerometer.getMovYValue())))
     			res = true;
         	break;
         case ARRIBA:
         	if(accelerometer.isPositiveMovY() && Math.abs(accelerometer.getMovYValue()) > minMov
         									  && Math.abs(accelerometer.getMovYValue())> (Math.abs(accelerometer.getMovZValue()))
         									  && Math.abs(accelerometer.getMovYValue())> (Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ABAJO:
         	if(accelerometer.isNegativeMovY() && Math.abs(accelerometer.getMovYValue()) > minMov 
         									  &&  Math.abs(accelerometer.getMovYValue())> (Math.abs(accelerometer.getMovZValue()))
         									  && Math.abs(accelerometer.getMovYValue())> (Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ACERCAR:
         	if(accelerometer.isPositiveMovZ() && Math.abs(accelerometer.getMovZValue()) > minMov
         									  && Math.abs(accelerometer.getMovZValue())> (Math.abs(accelerometer.getMovYValue()))
         									  && Math.abs(accelerometer.getMovZValue())> (Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ALEJAR:
         	if(accelerometer.isNegativeMovZ() && Math.abs(accelerometer.getMovZValue()) > minMov
         									  && Math.abs(accelerometer.getMovZValue())> (Math.abs(accelerometer.getMovYValue()))
         									  && Math.abs(accelerometer.getMovZValue())> (Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
		}
	    
    	if(res)
    		mostrarActualPambilDice();
    	
    	if(!res)
    		actualErrors++;
    	
    	return res;
    	
    }
    
    public void actualizarTimeImage(){
    	switch(timeImage){
    		case 4:
	    	case 3:
	    		timeNumImage.setImageResource(R.drawable.num_tres);
	    		break;
	    	case 2:
	    		timeNumImage.setImageResource(R.drawable.num_dos);
	    		break;
	    	case 1:
	    		timeNumImage.setImageResource(R.drawable.num_uno);
	    		break;
			default:
				timeNumImage.setVisibility(View.INVISIBLE);
				break;
    	}
    }
    
    public void mostrarActualPambilDice(){
    	
    	switch(secuenciaPambilDice[actualMovDice]){
	    	case ARRIBA:
	    		tipoMov = TipoMovimiento.ARRIBA;
	    		break;
	    	case ABAJO:
	    		tipoMov = TipoMovimiento.ABAJO;
	    		break;
	    	case ACERCAR:
	    		tipoMov = TipoMovimiento.ABAJOPROFUNDO;
	    		break;
	    	case ALEJAR:
	    		tipoMov = TipoMovimiento.ARRIBAPROFUNDO;
	    		break;
	    	case DERECHA:
	    		tipoMov = TipoMovimiento.DERECHA;
	    		break;
	    	case IZQUIERDA:
	    		tipoMov = TipoMovimiento.IZQUIERDA;
	    		break;
    	}
    	
    	changePambilImage();	
    }
    

    /**
	 * OnCreate Method Override 
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
     //   this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.one_player_mode);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        ((TextView) findViewById(R.id.txtMenu)).setText("Pambil Dice");
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ =  0f;
        accelerometerInitiated = false;
        
        secuenciaPambilDice=new int[50];
        for(int i=0;i<50;i++){
        	secuenciaPambilDice[i]=random.nextInt(5);
        }

        pambil = (ImageView) findViewById(R.id.pambil);
        timeNumImage = (ImageView) findViewById(R.id.timeNumImage);
        
        turnoText = (TextView) findViewById(R.id.TurnoText);
        
        setStateButton();   
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
	        		if(prevTurno == Turno.NADIE)
	        			turno = Turno.PAMBIL;
		        	stateApp = 1;  
		        		
	        	}else{	        		
	        		if(!jugadorDiceBien){ //restart
	        			turno = Turno.PAMBIL;
	        			jugadorDiceBien = true;
	        			timeImage = 4;
	        			actualizarTimeImage();
	        			actualMaxMovDice = 3;
	        			actualMovDice = 0;
	        			actualErrors = 0;
	        			secuenciaPambilDice=new int[50];
	        	        for(int i=0;i<50;i++){
	        	        	secuenciaPambilDice[i]=random.nextInt(5);
	        	        }
        	        
	        		}else{ //play
	        			stateApp = 0;
	        		}	
	        	} 	
	        	
	        	if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
	        	    
	        		if(stateApp == 1){
	        			stateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
	        			changePambilImage();
	        			changeFeedbackText();
	        		}else{
	        			stateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
	        			pambil.setImageResource(R.drawable.pambil_pause);
	        			((TextView) findViewById(R.id.opm_feedbackText)).setText("ZzZzZz...");
	        		}
	        			
	        	} else {
	        		
	        		if(stateApp == 1){
	        			stateButton.setBackground(getResources().getDrawable(R.drawable.pause));
	        			changePambilImage();
	        			changeFeedbackText();
	        		}else{
	        			stateButton.setBackground(getResources().getDrawable(R.drawable.play));	
	        			pambil.setImageResource(R.drawable.pambil_pause);
	        			((TextView) findViewById(R.id.opm_feedbackText)).setText("ZzZzZz...");
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
			case SLEEP:
				pambil.setImageResource(R.drawable.pambil_pause);
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
		switch(turno){
		case PAMBIL:
			((TextView) findViewById(R.id.opm_feedbackText)).setText("");
			//((TextView) findViewById(R.id.opm_feedbackText)).setText("Presta atención a lo que te dice Pambil");
			turnoText.setText("Turno de Pambil");
			turnoText.setTextColor(Color.parseColor("#ff0000"));
			turnoText.setVisibility(View.VISIBLE);
			break;
		case JUGADOR:
			((TextView) findViewById(R.id.opm_feedbackText)).setText("");
			//((TextView) findViewById(R.id.opm_feedbackText)).setText("Movimiento "+actualMovDice+" de "+(actualMaxMovDice-1));
			if(esperaJugador){
				turnoText.setText("Movimiento "+(actualMovDice)+" bien");
				turnoText.setTextColor(Color.parseColor("#00ff00"));
			}else{
				turnoText.setText("Tu turno "+actualMovDice+"/"+(actualMaxMovDice-1));
				turnoText.setTextColor(Color.parseColor("#ff0000"));
			}
			
			turnoText.setVisibility(View.VISIBLE);
			break;
		case ESPERAJUGADOR:
			turnoText.setText("BIEN ESPERA");
			break;
		case DERROTA:
			if(actualMaxMovDice<50){
				((TextView) findViewById(R.id.opm_feedbackText)).setText("Has conseguido recordar "+(actualMaxMovDice-1)+" movimientos\n"+"Pambil tiene mejor memoria que tú \n¡Intentálo de nuevo!");
				turnoText.setText("¡Has perdido!");
			}else{
				((TextView) findViewById(R.id.opm_feedbackText)).setText("Has conseguido recordar "+(actualMaxMovDice-1)+" movimientos\n"+"¡Eres un tramposillo! \n¡Hazlo bien!");
				turnoText.setText("¡Pierdes moralmente!");
			}
			turnoText.setTextColor(Color.parseColor("#000000"));
			turnoText.setVisibility(View.VISIBLE);
			break;
		default:
			((TextView) findViewById(R.id.opm_feedbackText)).setText("");
			break;
		}
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
