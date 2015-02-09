/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 09/02/2015
 * 
 * Utilizado material referido al uso de Geolocalización realizado por Jorge Chamorro Padial y Germán Iglesias Padial 
 * https://github.com/jorgechpugr/NPI-Android-Practica3
 * 
 * y de síntesis de voz realizado por Zoraida Callejas Carrión
 * http://github.com/zoraidacallejas
 * 
 */


package com.PambuDev.Gymcatna;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.regex.*;

import com.PambuDev.Utilities.MusicManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Esta clase es una activity que se encarga del reconocimiento de unas coordenadas en grados dadas por voz,
 * y su posterior búsqueda de ruta desde la posición actual hasta la dada 
 * @author Yusinho
 *
 */
public class AppGPSVoz extends Activity{

	
	// Default values for the language model and maximum number of recognition results
	// They are shown in the GUI when the app starts, and they are used when the user selection is not valid
	
	private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM; 
	
	
	private String languageModel = DEFAULT_LANG_MODEL; 
	
	private final int numResults = 1;
	
	private static final String LOGTAG = "ASRBEGIN";
	private static int ASR_CODE = 123;
	
	private Context context;
	private String cadenaReconocida, destino_lat, destino_lon;
	private double origen_lat,origen_lon, destino_lat_decimal, destino_lon_decimal;
		
	private LocationManager locManager;
	private LocationListener locListener;
	
	GoogleMap googleMap;
		
	MusicManager music;
		
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

        setContentView(R.layout.app_gps_voz);
        
        music = new MusicManager(context,"AppGPSVoz");
        music.loadAudioResources();

        bannerAd();
        
		setSpeakButton();
		setRutaButton();
		
		initGPSMap();
		comenzarLocalizacion();
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
	 * Sets up the listener for the button that the user
	 * must click to start talking
	 */
	@SuppressLint("DefaultLocale")
	private void setSpeakButton() {
		//Gain reference to speak button
		ImageView robot = (ImageView) findViewById(R.id.robot);
		//Button speak = (Button) findViewById(R.id.speech_btn);

		//Set up click listener
		robot.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Speech recognition does not currently work on simulated devices,
					//it the user is attempting to run the app in a simulated device
					//they will get a Toast
					((Button) findViewById(R.id.rutaButton)).setVisibility(View.INVISIBLE);
					if("generic".equals(Build.BRAND.toLowerCase())){
						Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
						toast.show();
						Log.d(LOGTAG, "ASR attempt on virtual device");						
					}
					else{
						listen(); 				//Set up the recognizer with the parameters and start listening
					}
				}
			});
	}
	
	/**
	 * Sets up the listener for the button that the user
	 * must click to start talking
	 */
	@SuppressLint("DefaultLocale")
	private void setRutaButton() {
		//Gain reference to speak button
		Button ruta = (Button) findViewById(R.id.rutaButton);
		
		//Set up click listener
		ruta.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+origen_lat+","+origen_lon+"&daddr="+destino_lat+destino_lon;
					
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
					startActivity(Intent.createChooser(intent, "Select an application"));
				}
			});
	}

	/**
	 * Initializes the speech recognizer and starts listening to the user input
	 */
	private void listen()  {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify language model
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

		// Specify how many results to receive
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numResults);  

		// Start listening
		startActivityForResult(intent, ASR_CODE);
    }
	
	/**
	 * Init GoogleMap with mapFragment
	 */
	public void initGPSMap(){
		//Obtenemos el fragment del mapa
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);

		//Inicializamos el objeto del fragmento
		googleMap = mapFragment.getMap();

		//Habilitamos MyLocation en el mapa
		googleMap.setMyLocationEnabled(true);

	}
	
	/**
	 *  Shows the formatted best of N best recognition results (N-best list) from
	 *  best to worst in the <code>ListView</code>. 
	 *  For each match, it will render the recognized phrase and the confidence with 
	 *  which it was recognized.
	 */
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE)  {
            if (resultCode == RESULT_OK)  {            	
            	if(data!=null) {
	            	//Retrieves the N-best list and the confidences from the ASR result
	            	ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					cadenaReconocida = nBestList.get(0);
					//Includes the collection in the ListView of the GUI
					setRecognizedText();
            	}
            }
        }
	}
	
	/**
	 * Aplica expresiones regulares para coger la información relevante de las coordenadas dichas por el usuario
	 */
	public void setRecognizedText(){
		//latitud( |\+|\-|[0-9]|\.|con|punto)*longitud( |\+|\-|[0-9]|\.|con|punto)*
		//latitud [0-9]+ grados? [0-9]+ minutos? [0-9|.|con]+ segundos? (norte|sur|este|oeste|n|s|e|w) longitud [0-9]+ grados? [0-9]+ minutos? [0-9|.]+ segundos? (norte|sur|este|oeste|n|s|e|w)
		//longitudValue.setText(cadenaReconocida);	
		String 
			   latitudStringP ="latitud[0-9]+grados?[0-9|once]+minutos?[0-9|con|.]+segundos?(norte|sur|este|oeste|n|s|e|w)", 
			   longitudStringP="longitud[0-9]+grados?[0-9|once]+minutos?[0-9|.|con]+segundos?(norte|sur|este|oeste|n|s|e|w)"; 
		
		Pattern allPattern = Pattern.compile(latitudStringP+longitudStringP),
				longitudPattern = Pattern.compile(longitudStringP),
				latitudPattern = Pattern.compile(latitudStringP);
		
		TextView feedback_text = (TextView) findViewById(R.id.text_fb);
		
	//	cadenaReconocida = "latitud 37 grados 11 minutos 48.4 segundos norte longitud 3 grados 37 minutos 28.8 segundos oeste";
		
		String cadenaSinEspacios = cadenaReconocida.replace(" ","");
		
		feedback_text.setText(cadenaReconocida+"\n"+cadenaSinEspacios);
		Matcher m = allPattern.matcher(cadenaSinEspacios);
		
		if(m.find()){
			Matcher mla = latitudPattern.matcher(cadenaSinEspacios);
			while (mla.find()) {
				destino_lat = mla.group();
				
				destino_lat = destino_lat.replace("latitud","")
					   .replace("grados","°")
					   .replace("minutos", "'")
					   .replace("segundos", "\"")
					   .replace("grado","°")
					   .replace("minuto", "'")
					   .replace("segundo", "\"")
					   .replace("punto",".")
					   .replace("con",".")
					   .replace("norte","N")
					   .replace("sur","S")
					   .replace("oeste","W")
					   .replace("este","E");
			}
			
			Matcher mlo = longitudPattern.matcher(cadenaSinEspacios);
			
			while (mlo.find()) {
				destino_lon = mlo.group();
				destino_lon = destino_lon.replace("longitud","")
					   .replace("grados","°")
					   .replace("minutos", "'")
					   .replace("segundos", "\"")
					   .replace("grado","°")
					   .replace("minuto", "'")
					   .replace("segundo", "\"")
					   .replace("punto",".")
					   .replace("con",".")
					   .replace("norte","N")
					   .replace("sur","S")
					   .replace("oeste","W")
					   .replace("este","E");
			}
			
			destino_lat_decimal = convertDegreesToDecimal(destino_lat);
			destino_lon_decimal = convertDegreesToDecimal(destino_lon);
			
			feedback_text.setText("(Latitud):"+destino_lat+"\n(Longitud):"+destino_lon);
		
			((Button) findViewById(R.id.rutaButton)).setVisibility(View.VISIBLE);
			addDestinationMarker();
		}else{
			Toast toast = Toast.makeText(getApplicationContext(),"No reconocido", Toast.LENGTH_SHORT);
			toast.show();
	
		}
	}
	
	
	/**
	 * Convierte coordenadas en grados a decimales (pierde precisión)
	 * @param degreesString cadena coordenada en grados a convertir
	 * @return coordenada decimal
	 */
	private double convertDegreesToDecimal(String degreesString){
		//dd = d + m/60 + s/3600
		String grados = "[0-9]+°",
			   minutos = "[0-9]+'",
			   segundos = "[0-9]+\"",
			   cardinal = "[N|S|W|E]",
			   partialRes = "";
		
		double grad_double = 0,
			   min_double = 0,
			   sec_double = 0,
			   cardinal_sign = 1,
			   finalRes = 0;
		
		Pattern patternGrados = Pattern.compile(grados),
				patternMinutos = Pattern.compile(minutos),
				patternSegundos = Pattern.compile(segundos),
				patternCardinal = Pattern.compile(cardinal);
		
		Matcher m = patternGrados.matcher(degreesString);
		
		while (m.find()) {
			partialRes = m.group();
			partialRes = partialRes.replace("°","");
			grad_double = Double.parseDouble(partialRes);
		}
		
		m = patternMinutos.matcher(degreesString);
		
		while (m.find()) {
			partialRes = m.group();
			partialRes = partialRes.replace("'","");
			min_double = Double.parseDouble(partialRes)/60.0;	
		}
		
		m = patternSegundos.matcher(degreesString);
		
		
		while (m.find()) {
			partialRes = m.group();
			partialRes = partialRes.replace("\"","");
			sec_double = Double.parseDouble(partialRes)/3600.0;
		}
		
		m = patternCardinal.matcher(degreesString);
		
		while (m.find()) {
			partialRes = m.group();
			if(partialRes.equals("N") || partialRes.equals("E")){
				cardinal_sign = 1;
			}else{
				cardinal_sign = -1;
			}
		}
		
		finalRes = (grad_double + min_double + sec_double)*cardinal_sign;
		
		return finalRes;		
	}
	
	/**
	 * Add a marker to destination in the map
	 */
	private void addDestinationMarker(){
		googleMap.addMarker(new MarkerOptions()
        .position(new LatLng(destino_lat_decimal,destino_lon_decimal))
        .title("Destination"));
	}
	
	/**
	Se obtiene el servicio de geolocalizacion. Se inserta la escucha activa de la geolocalizacion
	para que se actualice periodicamente
	*/
	private void comenzarLocalizacion() {
		// Obtenemos una referencia al LocationManager
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Obtenemos la última posición conocida
		Location loc = locManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// Nos registramos para recibir actualizaciones de la posición
		locListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				mostrarPosicion(location);
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}
		};
		
		// Mostramos la última posición conocida
		mostrarPosicion(loc);
		
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000,
				0, locListener);
		
	}
	
	/**
	Se muestra la localizacion en el mapa de Google
	@para loc objeto Location cuyas coordenadas seran mostradas en el mapa.
	*/
	private void mostrarPosicion(Location loc) {
		TextView feedback_text = (TextView) findViewById(R.id.text_fb);
		
		if (loc != null) {
			//Texto de localizacion

			//Obtenemos la latitud
			double lat = 
			origen_lat = loc.getLatitude();
				
			//Obtenemos la longitud
			double lon =
			origen_lon = loc.getLongitude();

			//Creamos un objeto latLng para usarlo en el mapa
			LatLng latLng = new LatLng(lat,lon);

			//Mostramos la localizacion en el mapa
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			
			//Hacemos zoom en el mapa
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15)); 
					
			((Button) findViewById(R.id.rutaButton)).setVisibility(View.VISIBLE);
			feedback_text.setText(lat+"\n"+lon);
			//feedback_text.setText(R.string.agv_fb_text2);
		
		}
		//feedback_text.setText("loc null");
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
					Intent i = new Intent( AppGPSVoz.this, AppGPSVoz.class );
                    i.putExtra("resultado","ok" );
                    setResult( Activity.RESULT_OK, i );
                    AppGPSVoz.this.finish();
					break;
					
			}
			return false;
		}
	
}
