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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	
	private InterstitialAd interstitial;
	
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
        
        setContentView(R.layout.menu_activity);
        music = new MusicManager(context,"Menu_Activity");
        music.loadAudioResources();
        
		bannerAd();
        interstitialAd();	
   
		setJuegoButton();
		setPruebasButton();
		setMapaButton();
		setOvilloButton();
		setBrujulaButton();
		setCofreButton();
		setIconMenuButton();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				if(mainState == MainState.PRUEBAS || mainState == MainState.JUEGO){
					mainState = MainState.INITIAL;
					ChangeLayout();
				}else{
					finishWithAd();
				}
				break;
				
		}
		return false;
	}
	
	/**
	 * set Banner Ad
	 */
	public void bannerAd(){
		RelativeLayout item = (RelativeLayout)findViewById(R.id.root_layout);
		View child = getLayoutInflater().inflate(R.layout.banner_ad, null);
		item.addView(child);
		 //Locate the Banner Ad in activity_main.xml
        AdView adView = (AdView) this.findViewById(R.id.adView);
     // Request for Ads
        AdRequest adRequest = new AdRequest.Builder().build();
        // Load ads into Banner Ads
        adView.loadAd(adRequest);
	}
	
	/**
	 * Set Interstitial Ad
	 */
	public void interstitialAd(){
		 // Crear el intersticial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-6684398297757883/3086971655");        
        AdRequest adRequest = new AdRequest.Builder().build();
        // Comenzar la carga del intersticial.
        interstitial.loadAd(adRequest);
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
					 //Toast.makeText(context, "Próximamente disponible", Toast.LENGTH_SHORT).show();
					 Intent i = new Intent(context,ModoJugar.class);	 	            
		 	         startActivity(i);
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
	
	 /**
	 * Sets up the listener for menu for the iconMenu button
	 */
    private void setIconMenuButton(){
    	ImageView infoButton = (ImageView) findViewById(R.id.menu_info);
    	
    	// Set up click listener
			infoButton.setOnClickListener(new OnClickListener() {              
		       
				@Override 
		        public void onClick(View v) {  
					 if(v.getId()==findViewById(R.id.menu_info).getId()){
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
            			R.string.creditos, Toast.LENGTH_LONG);
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
            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YusBravo/NPI/tree/master/Gymcatna")));
                res= true;
                break;
            case R.id.ExitOption:
            	 finishWithAd();
            	break;
            default:
                res= super.onContextItemSelected(item);
                break;
        }
        
        return res;		
    }
    
    /**
     * finish app with an ad is is loaded
     */
    private void finishWithAd(){
    	if(interstitial.isLoaded())
    		interstitial.show();
    	
    	finish();
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
