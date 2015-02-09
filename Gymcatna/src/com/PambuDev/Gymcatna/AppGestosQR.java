/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 09/02/2015
 * 
 * Utilizado material referido al uso de gestos realizados por
 *  Javier Escobar Cerezo  y Julio Rodríguez Martínez 
 * https://github.com/Jick9536/NPI-P3
 */

package com.PambuDev.Gymcatna;

import java.util.ArrayList;
import java.util.Random;

import com.PambuDev.Gymcatna.Menu_Activity.MainState;
import com.PambuDev.Utilities.IntentIntegrator;
import com.PambuDev.Utilities.IntentResult;
import com.PambuDev.Utilities.MusicManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.regex.*;


/**
 * Esta clase es una activity que se encarga del reconocimiento de una serie de patrones (letras m,l,n,z y v), y el posterior
 * lanzamiento de un intent a la app Barcode Scanner, para detectar códigos QR, manejando los datos de vuelta
 * @author Yusinho
 *
 */
public class AppGestosQR  extends Activity implements OnGesturePerformedListener{

	
	private GestureLibrary gestureLib;
	private String letter = "";
	MusicManager music;
	TextView longitudValue, latitudValue;
	String cadenaReconocida = "";
	Context context;
	boolean completed = false;
	
	Random random = new Random();
	
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
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        context = getBaseContext();
        
	    GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.app_gestos_qr, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		setContentView(gestureOverlayView);
		
		 music = new MusicManager(context,"AppGestosQR");
	     music.loadAudioResources();
		
		longitudValue = (TextView) findViewById(R.id.longitud_value);
        latitudValue = (TextView) findViewById(R.id.latitud_value);

		 if (!gestureLib.load()) 
		 {
			 finish();
		 }
		 
		 setOvilloScreen();
		 bannerAd();

	}
	
	/**
	 * Get Ovillo screen (drawable puzzle {l,m,v}) with random
	 */
	public void setOvilloScreen(){
		int r =random.nextInt(3);

		switch(r){
			case 0:
				letter = "m";
				((ImageView) findViewById(R.id.ovillo)).setImageResource(R.drawable.ovillo_m);
				break;
			case 1:
				letter = "l";
				((ImageView) findViewById(R.id.ovillo)).setImageResource(R.drawable.ovillo_l);
				break;
			case 2:
				letter ="v";
				((ImageView) findViewById(R.id.ovillo)).setImageResource(R.drawable.ovillo_v);
				break;
		}
		
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
	
	
	 /* Métodos para tratamiento de gestos. */
	 @Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture)
	 {
		 ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
		 for (Prediction prediction : predictions) 
		 {
			 //Otra opción es directamente coger el primer elemento que supere la predicción de 1.0
			 if (prediction.score > 5.0) 
			 {
				 if(prediction.name.equals(letter)){
					 ((TextView) findViewById(R.id.text_fb)).setText(R.string.agq_fb_text2);
					 completed = true;
					 iniciarEscaneoQR();
				 }else{
					 Toast.makeText(this,prediction.name.toString(), Toast.LENGTH_SHORT).show();
				 }
	 
			 }	 
		 }
		
	 }
	 
	/**
	 * Initiate QR scanner
	 */
	 public void  iniciarEscaneoQR(){
		 Toast.makeText(this, "inicia escaneo", Toast.LENGTH_SHORT).show();
		 IntentIntegrator integrator = new IntentIntegrator(this);
		 integrator.initiateScan();
	 }
	 
	 /**
	  * take QR scanner reults
	  */
	 public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		  if (scanResult != null) {
			
			  cadenaReconocida = scanResult.toString();
			  setRecognizedResults();
		  }
		  // else continue with any other code you need in the method
		  
		}
	 
	 /**
	  * manage QR scanner results
	  */
	 public void setRecognizedResults(){
		//LATITUD_-?[0-9]+.[0-9]+_LONGITUD_-?[0-9]+.[0-9]+
			
			String 
				   latitudStringP ="LATITUD_-?[0-9]+.[0-9]+", 
				   longitudStringP="LONGITUD_-?[0-9]+.[0-9]+"; 
			
			//pattern
			Pattern longitudPattern = Pattern.compile(longitudStringP),
					latitudPattern = Pattern.compile(latitudStringP);
			
				Matcher mla = latitudPattern.matcher(cadenaReconocida);
				while (mla.find()) {
					latitudValue.setText(mla.group().replace("LATITUD_", ""));
					latitudValue.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.latitud_text)).setVisibility(View.VISIBLE);
				}
				
				Matcher mlo = longitudPattern.matcher(cadenaReconocida);
				while (mlo.find()) {
					longitudValue.setText(mlo.group().replace("LONGITUD_", ""));
					longitudValue.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.longitud_text)).setVisibility(View.VISIBLE);
				}
	
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
		
		
		
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub

			switch(keyCode){
				case KeyEvent.KEYCODE_BACK:
					Intent i = new Intent( AppGestosQR.this, AppGestosQR.class );
					
					if(completed)
						i.putExtra("resultado","ok" );
					else
						i.putExtra("resultado","fail");
					
                    setResult( Activity.RESULT_OK, i );
                    AppGestosQR.this.finish();
					break;
					
			}
			return false;
		}
}
