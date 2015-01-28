/**
 * Título: Acelerómetro
 * Licencia Pública General de GNU (GPL) versión 3 
 * Autores:
 * - José Francisco Bravo Sánchez
 * - Pedro Fernández Bosch
 * Fecha de la última modificación: 06/01/2015
 */

package com.PambuDev.Interfaces;

public interface AccelerometerInterface {
	
	public float getAccelX();

    public float getAccelY();

    public float getAccelZ();

    public long getAtTime();
    
    public float getPower();
    
    /**
     * Actualize previous axis values
     */
    public void actPrevAxisValues();
    
    /**
     * Actualize movement values
     * @param timeDiff time needed to calculate between actual position and previous position
     */
    public void actAxisMov(long timeDiff);
    
    public boolean isPositiveMovX();
    
    public boolean isNegativeMovX();
    
    public boolean isPositiveMovY();
    
    public boolean isNegativeMovY();
    
    public boolean isPositiveMovZ();
    
    public boolean isNegativeMovZ();
    
    public float getTotalMov();
    
    public float getMovXValue();
    
    public float getMovYValue();
    
    public float getMovZValue();
}
