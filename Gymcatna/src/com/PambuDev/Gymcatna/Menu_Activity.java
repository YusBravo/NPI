/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 28/01/2015
 * 
 */
package com.PambuDev.Gymcatna;


import com.PambuDev.Utilities.MusicManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * Esta clase es una activity que es el menú principal de la aplicación para acceder a las distintas activities
 * @author Yusinho
 *
 */
public class Menu_Activity extends Activity {

	ImageView test;
	MusicManager music;
	
	int sdk = android.os.Build.VERSION.SDK_INT;
	Context context;
	
	public enum MainState{
		INITIAL, JUEGO, PRUEBAS
	}
	
	MainState mainState = MainState.INITIAL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
      //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
        context = getBaseContext();
        
        music = new MusicManager(context,"Menu_Activity");
        music.loadAudioResources();
        
		setContentView(R.layout.menu_activity);
		
		
		
		setJuegoButton();
		setPruebasButton();
		setMapaButton();
		setOvilloButton();
		setBrujulaButton();
		setCofreButton();
	}
	
	
	
	 /**
	 * Sets up the listener for the juego button 
	 */
	private void setJuegoButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_juego);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_juego).getId()){
		 	           // Intent i = new Intent(context,AppMovimientoSonido.class);	 	            
		 	            //startActivity(i);
					 Toast.makeText(context, "Próximamente disponible", Toast.LENGTH_SHORT).show();		 
		         }
	        }  
	    });
	}
	
	 /**
	 * Sets up the listener for the pruebas button 
	 */
	private void setPruebasButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_pruebas);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_pruebas).getId()){
		 	           // Intent i = new Intent(context,AppMovimientoSonido.class);	 	            
		 	            //startActivity(i);
					 //Toast.makeText(context, "Próximamente disponible", Toast.LENGTH_SHORT).show();
					 mainState = MainState.PRUEBAS;
					 ChangeLayout();
		         }
	        }  
	    });
	}
	
	/**
	 * Change layout according mainState
	 */
	private void ChangeLayout(){
		switch(mainState){
			case INITIAL:
				findViewById(R.id.pruebas_layout).setVisibility(View.GONE);
				findViewById(R.id.initial_layout).setVisibility(View.VISIBLE);
				break;
			case PRUEBAS:
				findViewById(R.id.initial_layout).setVisibility(View.GONE);
				findViewById(R.id.pruebas_layout).setVisibility(View.VISIBLE);
				break;
			case JUEGO:
			default:
				findViewById(R.id.pruebas_layout).setVisibility(View.GONE);
				findViewById(R.id.initial_layout).setVisibility(View.VISIBLE);
				break;
		}
		
	}
	
	 /**
	 * Sets up the listener for the cofre button 
	 */
	private void setCofreButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_cofre);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_cofre).getId()){
		 	            Intent i = new Intent(context,AppMovimientoSonido.class);	 	            
		 	            startActivity(i);
		         }
	        }  
	    });

	}
	
	 /**
	 * Sets up the listener for the brujula button 
	 */
	private void setBrujulaButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_brujula);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_brujula).getId()){
		 	            Intent i = new Intent(context,AppFotoBrujula.class);	 	            
		 	            startActivity(i);
		         }
	        }  
	    });

	}
	
	 /**
	 * Sets up the listener for the cofre button 
	 */
	private void setOvilloButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_ovillo);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_ovillo).getId()){
		 	            Intent i = new Intent(context,AppGestosQR.class);	 	            
		 	            startActivity(i);
		         }
	        }  
	    });

	}
	
	 /**
	 * Sets up the listener for the cofre button 
	 */
	private void setMapaButton() {
		// Reference the speak button
		ImageView button = (ImageView) findViewById(R.id.menu_mapa);

		// Set up click listener
		button.setOnClickListener(new OnClickListener() {              
	       
			@Override 
	        public void onClick(View v) {  
				 if(v.getId()==findViewById(R.id.menu_mapa).getId()){
		 	            Intent i = new Intent(context,AppGPSVoz.class);	 	            
		 	            startActivity(i);
		         }
	        }  
	    });

	}
	
	@Override
	protected void onPause(){
		super.onPause();
		
		music.Pause();
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		music.Resume();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		music.Dispose();
	}
}
