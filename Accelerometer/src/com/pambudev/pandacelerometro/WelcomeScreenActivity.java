/**
 * Título: Acelerómetro
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autores:
 * - José Francisco Bravo Sánchez (Yus Bravo)
 * - Pedro Fernández Bosch
 * Fecha de la última modificación: 08/01/2015
 */

package com.pambudev.pandacelerometro;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.pambudev.accelerometer.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class WelcomeScreenActivity extends Activity {

	/**
	 * Declaración de variables 
	 */
	Accelerometer accelerometer;
    Float accelX, accelY, accelZ;
    long lastUpdate, lastMov;
    int sdk = android.os.Build.VERSION.SDK_INT;
    int deltaTime = 40;
    boolean accelerometerInitiated;
    private Button onePlayerModeButton,
    			   twoPlayersModeButton,
    			   testModeButton,
    			   iconMenuButton;
    private ImageView pambil;
    private int stateApp = 1;
    int cont = 0;
    public enum TipoMovimiento{
    	NINGUNO, ARRIBA, ABAJO, DERECHA, IZQUIERDA, ARRIBAPROFUNDO,ABAJOPROFUNDO
    }
    
    private TipoMovimiento tipoMov = TipoMovimiento.NINGUNO;

    
    private InterstitialAd interstitial;

    
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

        setContentView(R.layout.welcome_screen);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ =  0f;
        accelerometerInitiated = false;

        pambil = (ImageView) findViewById(R.id.pambil);
    
       
        setTestModeButton();          
        setIconMenuButton();
        setOnePlayerModeButton();
        setTwoPlayersModeButton();
    
     // Crear el intersticial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-6684398297757883/2434496857");
        
        // Crear la solicitud de anuncio.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Comenzar la carga del intersticial.
        interstitial.loadAd(adRequest);
        
    }
	
    // Invoca displayInterstitial() cuando esté preparado para mostrar un intersticial.
    public void displayInterstitial() {
      if (interstitial.isLoaded()) {
        interstitial.show();
      }
     
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
	 * Sets up the listener for the arriba button that the user
	 * must click to change tipoMov to arriba 
	 */
	private void setTestModeButton() {
		// Reference the speak button
		testModeButton = (Button) findViewById(R.id.ws_testButton);

		// Set up click listener
		testModeButton.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.ws_testButton).getId()){
					 	displayInterstitial();
		 	            Intent i = new Intent(getBaseContext(),MainActivity.class);	 	            
		 	            startActivity(i);
		         }
	        }  
	    });

	}
	
	 /**
		 * Sets up the listener for the arriba button that the user
		 * must click to change tipoMov to arriba 
		 */
		private void setOnePlayerModeButton() {
			// Reference the speak button
			onePlayerModeButton = (Button) findViewById(R.id.ws_onePlayerButton);

			// Set up click listener
			onePlayerModeButton.setOnClickListener(new OnClickListener() {              
		       
				@Override 
		        public void onClick(View v) {  
					 if(v.getId()==findViewById(R.id.ws_onePlayerButton).getId()){
						    displayInterstitial();
			 	            Intent i = new Intent(getBaseContext(),OnePlayerActivity.class);	 	            
			 	            startActivity(i);
			         }
		        }  
		    });

		}
	
		/**
		 * Sets up the listener for the arriba button that the user
		 * must click to change tipoMov to arriba 
		 */
		private void setTwoPlayersModeButton() {
			// Reference the speak button
			twoPlayersModeButton = (Button) findViewById(R.id.ws_twoPlayersButton);

			// Set up click listener
			twoPlayersModeButton.setOnClickListener(new OnClickListener() {              
		       
				@Override 
		        public void onClick(View v) {  
					/* if(v.getId()==findViewById(R.id.ws_onePlayerButton).getId()){
			 	            Intent i = new Intent(getBaseContext(),OnePlayerActivity.class);	 	            
			 	            startActivity(i);
			         }*/
					
					Toast toast;
					toast = Toast.makeText(getApplicationContext(),
	            			"Próximamente disponible", Toast.LENGTH_SHORT);
	            	toast.setDuration(Toast.LENGTH_SHORT);
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
	    
	/**
	 * Create Context Menu
	 */
/*	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo)
	{
	    super.onCreateContextMenu(menu, v, menuInfo);
	 
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	    
	   
	}
	*/
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
