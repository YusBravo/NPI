/**
 * Título: Gymcatna
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autor:
 * José Francisco Bravo Sánchez (Yus Bravo)
 * Fecha de la última modificación: 28/01/2015
 * 
 */

package com.PambuDev.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.PambuDev.Gymcatna.R;

/**
 * Esta clase maneja los recursos de MediaPlayer de cada activity
 *
 */
public class MusicManager {

	String id_activity;
	MediaPlayer mainMusic;
	Context context;
	
	public MusicManager(Context cont, String id){
		id_activity = id;
		context = cont;
	}
	
	@SuppressLint("NewApi")
	public void loadAudioResources(){
		if(id_activity =="Menu_Activity"){
			
				mainMusic = MediaPlayer.create(context, R.raw.main_theme);			
		}else{
				mainMusic = MediaPlayer.create(context, R.raw.minijuegos_theme);
		
		}
		
		mainMusic.start();
		mainMusic.setLooping(true);
	}
	
	public void Pause(){
		
		try{
			if(mainMusic.isPlaying())
				mainMusic.pause();
		}catch(Exception e){
			
		}	
	}
	
	public void Resume(){
		try{
			mainMusic.start();
		}catch(Exception e){
			
		}
	
	}
	
	public void Dispose(){

		try{
			mainMusic.release();
		}catch(Exception e){
			
		}

	}
}
