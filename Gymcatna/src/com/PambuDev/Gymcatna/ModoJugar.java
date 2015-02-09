/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 28/01/2015
 */
package com.PambuDev.Gymcatna;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.widget.Button;
import android.widget.TextView;

/**
 * Esta clase es una activity que se encarga de mostrar el logo de PambuDev durante un deltaTime
 * @author Yusinho
 *
 */
public class ModoJugar extends Activity {

	/**
	 * Declaración de variables 
	 */
    int deltaTime = 3000;
    Context context;
    boolean player_loose = false;
    Random random = new Random();
    int totalGames = 4;
    int fases_completadas = 0;
    int REQUEST_CODE = 0;
    int last_minigame = -1;

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
        
        setContentView(R.layout.modo_jugar);

        nuevaFase();
    }
    
    /**
     * elige random nueva fase sin repetir anterior y la lanza tras deltatime
     */
    public void nuevaFase(){
    	((Button) findViewById(R.id.totalGames)).setText(""+fases_completadas);
    	
    	if(fases_completadas>0)
    		((TextView) findViewById(R.id.text_of_challenge)).setText(R.string.continua_juego);
    	
    	 TimerTask task = new TimerTask() {
             @Override
             public void run() {
  
             	int r = random.nextInt(totalGames);
             	
             	//evitamos repetir minijuego
             	while(r == last_minigame){
             		r = random.nextInt(totalGames);
             	}
             	
             	
             	last_minigame = r;
             	
             	Intent i;
             
             	switch(r){
 	            	case 0:
 	            		i = new Intent(context,AppGestosQR.class);
 	            		break;
 	            	case 1:
 	            		i = new Intent(context,AppGPSVoz.class);
 	            		break;
 	            	case 2:
 	            		i = new Intent(context,AppFotoBrujula.class);
 	            		break;
 	            	case 3:
 	            		i = new Intent(context,AppMovimientoSonido.class);
 	            		break;
 	            	default:
 	            		i = new Intent(context,AppGestosQR.class);
 	            		break;
             	}
             	
             	
                 // Start the next activity
                 
             	i.putExtra("fases_completadas", fases_completadas);
                ModoJugar.this.startActivityForResult(i,REQUEST_CODE);
             }
         };
  
         // Simulate a long loading process on application startup.
         Timer timer = new Timer();
         timer.schedule(task, deltaTime);
    }
    
    /**
     * método cuando pierde el jugador, cambia feedback text y finaliza tras deltatime
     */
    public void finJuego(){
    	((TextView) findViewById(R.id.text_of_challenge)).setText(R.string.fin_juego);
    	
    	 TimerTask task = new TimerTask() {
             @Override
             public void run() {
  
                 	finish();
                
             }
         };
  
         // Simulate a long loading process on application startup.
         Timer timer = new Timer();
         timer.schedule(task, deltaTime);
    }
    
    /**
     * Recoje datos de las activities lanzadas para comprobar su resultado
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if ( requestCode == 0 ){
              if ( resultCode == Activity.RESULT_OK){

                  String res = data.getExtras().get("resultado").toString();
                 
                  if(res.equals("fail")){
                	  player_loose = true;
                  }else if(res.equals("ok")){
                	  fases_completadas++;
                  }
                  		
                  if(!player_loose)
                	  nuevaFase();
                  else{
                	  finJuego();
                  }
              }
         }

    }
    
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				player_loose = true;
				break;
				
		}
		return false;
	}

	
    @Override
    protected void onResume() {
        super.onResume();
       
    }

}
