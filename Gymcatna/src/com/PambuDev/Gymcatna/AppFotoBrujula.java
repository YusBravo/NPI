/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 28/01/2015
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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        
        music = new MusicManager(context,"AppFotoBrujula");
        music.loadAudioResources();
        
        cam = new CameraController(this);
	    
	    setContentView(R.layout.app_foto_brujula);
	    
	    //Cargamos la imagen
  		brujula = (ImageView) findViewById(R.id.brujula);

  		//Obtenemos el campo a controlar
  		brujulaText = (TextView) findViewById(R.id.brujulaText);

		//Inciamos el manager de sensores
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		setSpinner();
		
		
	}
	
	/**
	 * Set spinner to select cardinal point
	 */
	public void setSpinner(){
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] valores = {"norte","este","sur","oeste"};
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
 
        brujulaText.setText("Brújula: " + Float.toString(grado) + " grados");
 
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
	
	private boolean CheckPuntoCardinal(float grado){
		boolean checked = false;
		
		if(puntoCardinal == "sur")
			if((grado%360) == 180)
				checked = true;
		else if(puntoCardinal== "norte")
			if((grado%360) == 0)
				checked = true;
		else if(puntoCardinal == "este")
			if((grado%360) == 90)
				checked = true;
		else if(puntoCardinal == "oeste")
			if((grado%360) == 270)
				checked = true;

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
}
