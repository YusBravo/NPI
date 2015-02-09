/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 09/02/2015
 * 
 * Utilizado material referido al uso de brújula realizado por Jorge Chamorro Padial y Germán Iglesias Padial
 * https://github.com/jorgechpugr/NPI-Android-Practica3
 *  
 * y de cámara realizado por  Eva María Almansa Aránega, Luis Alberto Segura Delgado y Samuel López Liñán 
 * https://github.com/segura2010/Nuevos-Paradigmas-de-Interaccion/tree/master/P3
 * 
 */

package com.PambuDev.Gymcatna;

import com.PambuDev.Utilities.CameraController;
import com.PambuDev.Utilities.MusicManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;


/**
 * Esta clase es una activity que se encarga del la búsqueda de un punto cardinal dado por el spinner,
 * y el posterior lanzamiento de la cámara de fotos del móvil
 * @author Yusinho
 *
 */
public class AppFotoBrujula extends Activity  implements SensorEventListener{
	
	//Definimos la imagen de la brujula
		private ImageView brujula;

		//Se encarga del angulo de la brujula
		private float gradoActual = 0f;

		//Maganer de sensores
		private SensorManager mSensorManager;
		
		private String puntoCardinal = "";
		
		CameraController cam;
		Context context;

		//Definimos el textview
		TextView brujulaText;
		
		MusicManager music;
		
		boolean ubicado = false;
	
		
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
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        
        context = getBaseContext();
        
        cam = new CameraController(this);
	    
	    setContentView(R.layout.app_foto_brujula);
	    
	    music = new MusicManager(context,"AppFotoBrujula");
        music.loadAudioResources();
        
	    //Cargamos la imagen
  		brujula = (ImageView) findViewById(R.id.brujula);

  		//Obtenemos el campo a controlar
  		brujulaText = (TextView) findViewById(R.id.brujulaText);

		//Inciamos el manager de sensores
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		bannerAd();
		setSpinner();
		
		
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
	 * Set spinner to select cardinal point
	 */
	public void setSpinner(){
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] valores = {"Select One","N","E","S","W"};
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valores));
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
            	puntoCardinal = (String) adapterView.getItemAtPosition(position);
            	ubicado = false;
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
                 
            }
        });
	}
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
         
        music.Resume();
        //Registramos el listener del sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        
     //  Toast.makeText(this, "registrado", Toast.LENGTH_SHORT).show();
    }
	
	
	/**
	Cuando se pasa a segundo plano. No necesitamos escuchar al sensor. 
	Desregistramos el sensor.
	*/
	@Override
    protected void onPause() {
        super.onPause();
        music.Pause();
        
        //Parar el listener
        mSensorManager.unregisterListener(this);
     //   Toast.makeText(this, "desregistrado", Toast.LENGTH_SHORT).show();
    }

	
	/**
	Actualiza los valores de la brujula

	@param event Contiene informacion del cambio de orientacion de la brujula
	*/
	@Override
	public void onSensorChanged(SensorEvent event) {
		// Toast.makeText(this, "sensorChanged", Toast.LENGTH_SHORT).show();
		//Obtener el angulo sobre el eje Z
        float grado = Math.round(event.values[0]);
 
       // brujulaText.setText("Brújula: " + Float.toString(grado) + " grados");
 
        //Crear la animacion para rotar la imagen (Giro inverso de los grados)
        RotateAnimation ra = new RotateAnimation(
        		gradoActual,
                grado,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
 
        //Duracion de la animacion
        ra.setDuration(210);
 
        //Establecer la animacion
        ra.setFillAfter(true);
 
        //Iniciar la animacion
        brujula.startAnimation(ra);
        gradoActual = grado;
        
        if(CheckPuntoCardinal(gradoActual) && !ubicado){
        	cam.captureImage();
        	ubicado = true;
        }

	}
	
	/**
	 * comprueba si el punto cardinal seleccionado en el spinner coincide con los grados actuales 
	 * @param grado
	 * @return
	 */
	private boolean CheckPuntoCardinal(float grado){
		boolean checked = false;
		
		if(puntoCardinal == "S"){
			if((grado%360) > 176 && (grado%360) < 184 )
				checked = true;
		}else if(puntoCardinal== "N"){
			if(((grado%360) > 356 && (grado%360) < 361) || ((grado%360) >=0 && (grado%360) < 4))
				checked = true;
		}else if(puntoCardinal == "E"){
			if((grado%360) > 86 && (grado%360) < 94)
				checked = true;
		}else if(puntoCardinal == "W"){
			if((grado%360) > 266 && (grado%360) < 274)
				checked = true;
		}

		brujulaText.setText("Brújula: " + Float.toString(grado) + " grados\n Puntu cardinal" +puntoCardinal+checked );

		return checked;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
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
				Intent i = new Intent( AppFotoBrujula.this, AppFotoBrujula.class );
				
				if(ubicado)
					i.putExtra("resultado","ok");
				else
					i.putExtra("resultado","fail");
				
                setResult( Activity.RESULT_OK, i );
                AppFotoBrujula.this.finish();
				break;
				
		}
		return false;
	}
}
