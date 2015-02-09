/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 09/02/2015
 * 
 */
package com.PambuDev.Gymcatna;


import java.util.Random;

import com.PambuDev.Utilities.Accelerometer;
import com.PambuDev.Utilities.MusicManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Esta clase es una activity que se encarga del reconocimiento un patrón de movimiento realizado con el aceleróetro y giroscopio
 * del smartphone. Una vez reconocido, se ejecuta un sonido. El patron se puede observar en la variable movPatron
 * @author Yusinho
 *
 */
public class AppMovimientoSonido extends Activity {

	/**
	 * Declaración de variables 
	 */
	int sdk = android.os.Build.VERSION.SDK_INT;
	Context context;
	SoundPool soundPool;
	MusicManager music;
	int cofre;
	boolean desbloqueando = true;
	
	
	Accelerometer accelerometer;
    Float accelX, accelY, accelZ;
    long lastUpdate, lastMov, lastPatronMov;
    int timeBetweenPatronMov = 1000000000; //1 segundo
    final Handler myHandler = new Handler();
    int deltaTime = 40;
    boolean accelerometerInitiated;
   
    private int stateApp = 1;
    int cont = 0;
    public enum TipoMovimiento{
    	NINGUNO, ARRIBA, ABAJO, DERECHA, IZQUIERDA, ALEJAR,ACERCAR, GIRODERECHA, GIROIZQUIERDA
    }
    
  
    //private TipoMovimiento [] movPatron;
    Random random = new Random(); 
    private int actualPatronMov = 0;

    private TipoMovimiento [][] movPatron = {
    											{TipoMovimiento.ALEJAR, TipoMovimiento.ARRIBA,TipoMovimiento.DERECHA,TipoMovimiento.ABAJO, TipoMovimiento.GIRODERECHA},
    											{TipoMovimiento.ALEJAR, TipoMovimiento.IZQUIERDA,TipoMovimiento.ABAJO,TipoMovimiento.DERECHA, TipoMovimiento.GIRODERECHA}
    										};
    
    int indexPatron = 0;
    /**
	 * OnCreate Method Override 
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        actualPatronMov = 0;
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        context = getBaseContext();
       
        setContentView(R.layout.app_movimiento_sonido);
        
        music = new MusicManager(context,"AppMovimientoSonido");
        music.loadAudioResources();

        accelerometer = new Accelerometer(this);
        accelX = accelY = accelZ =  0f;
        accelerometerInitiated = false;
  
        setPatronCerradura();
        bannerAd();
        
        loadSoundResources();
        sensorThread(); //Thread simultaneo para manejo del acelerometro (sensorEventListener)

    }
    
    
   /**
    * set by random cerradura type
    */
    public void setPatronCerradura(){
    	int r = random.nextInt(2);
    	
    	indexPatron = r;
    	
    	switch(r){
	    	case 0:
	    		((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura0);
	    		break;
	    	case 1:
	    		((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_0);
	    		break;
    	}
    	
    	
    }
    
    /**
     * create thread for accelerometer and postactions
     */
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
                int limit = 50000000; //0,035 segundos
                float minMov = 1E-6f;
                float mov, movX, movY, movZ;
                long timeDiff;
                
                if(actualPatronMov < movPatron[indexPatron].length){
	                ChangeFeedbackText();
	                ChangeCerradura();
                }
                
                accelX = accelerometer.getAccelX();
                accelY = accelerometer.getAccelY();
                accelZ = accelerometer.getAccelZ();

                if (!accelerometerInitiated) {
                    lastUpdate = currentTime;
                    lastMov = currentTime;
                    lastPatronMov = currentTime;
                    accelerometer.actPrevAxisValues();
                    accelerometerInitiated = true;
                }

                timeDiff = currentTime - lastUpdate;
               
                if (timeDiff > 0 && desbloqueando) {
                	
                    if (currentTime - lastMov >= limit) { //intervalo en el cual realizar las comprobaciones
                    	
                    	accelerometer.actAxisMov(timeDiff);
                    	mov = accelerometer.getTotalMov();
                    	movX = accelerometer.getMovXValue();
                    	movY = accelerometer.getMovYValue();
                    	movZ = accelerometer.getMovZValue();
                    	 
                    	
                    		if(movimientoBienHecho() && (currentTime - lastPatronMov > timeBetweenPatronMov)){
                    			lastPatronMov = currentTime;
                    			actualPatronMov++;
                    			if(actualPatronMov == (movPatron[indexPatron].length)){
                    				desbloqueando = false;
                    				ChangeFeedbackText();
                    				((ImageView) findViewById(R.id.cerradura)).setVisibility(View.GONE);
                    				((ImageView) findViewById(R.id.cofre_cerrado)).setVisibility(View.GONE);
                    				((ImageView) findViewById(R.id.cofre_abierto)).setVisibility(View.VISIBLE);
                    				((ImageView) findViewById(R.id.sardina)).setVisibility(View.VISIBLE);
                    				//finish();
                    				soundPool.play(cofre, 1, 1, 0, 0, 1);
                    			}
                    		}
                    		
                        lastMov = currentTime;
                    }
                    
                   
        	        accelerometer.actPrevAxisValues();
                    lastUpdate = currentTime;
                }
            }
        }
    };
    
    
    
    /**
     * change feedback text 
     */
    public void ChangeFeedbackText(){
    	TextView textoGato = (TextView) findViewById(R.id.text_fb);
    	
    	switch(actualPatronMov){
	    	case 0:
	    			textoGato.setText(R.string.ams_fb_text_alejar);
	    		break;
	    	case 1:
	    		if(indexPatron == 0)
	    			textoGato.setText(R.string.ams_fb_text_arriba);
	    		else if(indexPatron == 1)
	    			textoGato.setText(R.string.ams_fb_text_izquierda);
	    		break;
	    	case 2:
	    		if(indexPatron == 0)
	    			textoGato.setText(R.string.ams_fb_text_derecha);
	    		else if(indexPatron == 1)
	    			textoGato.setText(R.string.ams_fb_text_abajo);
	    		break;
	    	case 3:
	    		if(indexPatron == 0)
	    			textoGato.setText(R.string.ams_fb_text_abajo);
	    		else if(indexPatron == 1)
	    			textoGato.setText(R.string.ams_fb_text_derecha);
	    		break;
	    	case 4:
	    			textoGato.setText(R.string.ams_fb_text_giroderecha);
	    		break;
			default:
					textoGato.setText(R.string.ams_fb_textfinal);
				break;
	    }
    }
    
    
    /**
     * CHange cerradura image
     */
    public void ChangeCerradura(){
 
    	switch(actualPatronMov){
	    	case 0:
	    		if(indexPatron == 0)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura0);
	    		else if(indexPatron == 1)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_0);
	    		break;
	    	case 1:
	    		if(indexPatron == 0)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura1);
	    		else if(indexPatron == 1)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_1);
	    		break;
	    	case 2:
	    		if(indexPatron == 0)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2);
	    		else if(indexPatron == 1)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_2);
	    		break;
	    	case 3:
	    		if(indexPatron == 0)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura3);
	    		else if(indexPatron == 1)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_3);
	    		break;
	    	case 4:
	    		if(indexPatron == 0)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura4);
	    		else if(indexPatron == 1)
	    			((ImageView) findViewById(R.id.cerradura)).setImageResource(R.drawable.cerradura2_4);
	    		break;
			default:
				break;
    	}
    }
    
    /**
     * set margins of a view object 
     * @param v view
     * @param l left
     * @param t top
     * @param r right
     * @param b bottom
     */
    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
    
    /**
     * 
     * Check if is correct the movement patron according actual
     * @return check boolean true or false
     */
    public boolean isCorrectMovPatron(){
    	boolean correct = false;
    	
    	switch(movPatron[indexPatron][actualPatronMov]){
	    	 case DERECHA:
	         	if(accelerometer.isPositiveMovX())
	     			correct = true;
	         	break;
	         case IZQUIERDA:
	     		if(accelerometer.isNegativeMovX())
	     			correct = true;
	         	break;
	         case ARRIBA:
	         	if(accelerometer.isPositiveMovY())
	         		correct = true;
	         	break;
	         case ABAJO:
	         	if(accelerometer.isNegativeMovY())
	         		correct = true;
	         	break;
	         case ACERCAR:
	         	if(accelerometer.isPositiveMovZ())
	         		correct = true;
	         	break;
	         case ALEJAR:
	         	if(accelerometer.isNegativeMovZ())
	         		correct = true;
	         	break;
	         case GIRODERECHA:
	        	 if(accelerometer.getAccelX() < -7)
	        		 correct = true;
	        	 break;
	         default:
	         	break;
    	}
    	
    	return correct;
    }
    
    public boolean movimientoBienHecho(){
    	boolean res = false;
    	
    	float minMov = 1E-7f;
    	float error = 0.8f;
    	
    	switch(movPatron[indexPatron][actualPatronMov]){
    	 case DERECHA:
         	if(accelerometer.isPositiveMovX()  && Math.abs(accelerometer.getMovXValue()) > minMov
         									   && Math.abs(accelerometer.getMovXValue())> (error*Math.abs(accelerometer.getMovZValue()))
         									   && Math.abs(accelerometer.getMovXValue())> (error*Math.abs(accelerometer.getMovYValue())))
     			res = true;
         	break;
         case IZQUIERDA:
     		if(accelerometer.isNegativeMovX()   && Math.abs(accelerometer.getMovXValue()) > minMov
     											&& Math.abs(accelerometer.getMovXValue())> (error*Math.abs(accelerometer.getMovZValue()))
     										    && Math.abs(accelerometer.getMovXValue())> (error*Math.abs(accelerometer.getMovYValue())))
     			res = true;
         	break;
         case ARRIBA:
         	if(accelerometer.isPositiveMovY() && Math.abs(accelerometer.getMovYValue()) > minMov
         									  && Math.abs(accelerometer.getMovYValue())> (error*Math.abs(accelerometer.getMovZValue()))
         									  && Math.abs(accelerometer.getMovYValue())> (error*Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ABAJO:
         	if(accelerometer.isNegativeMovY() && Math.abs(accelerometer.getMovYValue()) > minMov 
         									  &&  Math.abs(accelerometer.getMovYValue())> (error*Math.abs(accelerometer.getMovZValue()))
         									  && Math.abs(accelerometer.getMovYValue())> (error*Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ACERCAR:
         	if(accelerometer.isPositiveMovZ() && Math.abs(accelerometer.getMovZValue()) > minMov
         									  && Math.abs(accelerometer.getMovZValue())> (error*Math.abs(accelerometer.getMovYValue()))
         									  && Math.abs(accelerometer.getMovZValue())> (error*Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case ALEJAR:
         	if(accelerometer.isNegativeMovZ() && Math.abs(accelerometer.getMovZValue()) > minMov
         									  && Math.abs(accelerometer.getMovZValue())> (error*Math.abs(accelerometer.getMovYValue()))
         									  && Math.abs(accelerometer.getMovZValue())> (error*Math.abs(accelerometer.getMovXValue())))
         		res = true;
         	break;
         case GIRODERECHA:
        	 if(accelerometer.getAccelX() < -7)
        		 res = true;
        	 break;
         	default:
         		break;
		}
	
    	return res;
    	
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
     * Load sound resources to use in this activity
     */
	@SuppressLint("NewApi")
	public void loadSoundResources(){
    //	if(sdk < android.os.Build.VERSION_CODES.LOLLIPOP) {
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC , 1);
			
	/*	}else{
			soundPool = new SoundPool.Builder().build();
		}
    	*/
		cofre = soundPool.load(context, R.raw.open, 0);
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
				Intent i = new Intent( AppMovimientoSonido.this, AppMovimientoSonido.class );
				
				if(!desbloqueando){
					i.putExtra("resultado","ok");
				}else{
					i.putExtra("resultado","fail");
				}
				
                setResult( Activity.RESULT_OK, i );
                AppMovimientoSonido.this.finish();
				break;
				
		}
		return false;
	}

}

