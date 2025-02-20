package ubu.inf.gps.vista;

import java.util.Date;

import ubu.inf.gps.accesodatos.FachadaCoordenadas;

import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
/**
 * Servicio que se encarga de obtener las posiciones del GPS y guardarlas en una base de datos interna. Adem�s puede borrar todos los datos de los servidores y notificaciones de las otras aplicaciones.
 * @author  David Herrero de la Pe�a
 * @author  Jonatan Santos Barrios
 * @version  1.0
 * @see  Service
 */
public class ServicioGPS extends Service{
	boolean conectado = false;
	Thread hilo;
	LocationManager miLocationManagerGPS;
	LocationManager miLocationManager3G;
	Location miLocationGPS;
	Location miLocation3G;
	/**
	 * @uml.property  name="miLocationListenerGPS"
	 * @uml.associationEnd  
	 */
	MiLocationListenerGPS miLocationListenerGPS;
	/**
	 * @uml.property  name="miLocationListener3G"
	 * @uml.associationEnd  
	 */
	MiLocationListener3G miLocationListener3G;
	Location localizacion = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("gps","onCreate ServicioGPS");
		miLocationManagerGPS = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		miLocationManager3G = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if (miLocationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//si hay gps
			Log.i("gps","gps conectado, nos suscribimos");
			conectado=true;
			miLocationListenerGPS = new MiLocationListenerGPS();
			//TODO demasiado poco tiempo (asa,tiempo minimo en milis,distancia minima en metros,)
			miLocationManagerGPS.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER, 5*60*1000, 5, miLocationListenerGPS);			
		}
		if(miLocationManager3G.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			
			Log.i("gps","3G conectado, nos suscribimos");
			conectado=true;
			miLocationListener3G = new MiLocationListener3G();
			//TODO demasiado poco tiempo (asa,tiempo minimo en milis,distancia minima en metros,)
			miLocationManager3G.requestLocationUpdates(
	                LocationManager.NETWORK_PROVIDER,  5*60*1000, 5, miLocationListener3G);
			
		}
	}
	
	@Override
	public void onDestroy(){
		
		if(conectado){
			Log.i("gps","servicioGps,eliminamos la suscripcion");
			if(miLocationListenerGPS!=null){
				miLocationManagerGPS.removeUpdates(miLocationListenerGPS);
			}
			if(miLocationListener3G!=null){
				miLocationManager3G.removeUpdates(miLocationListener3G);
			}
		
		conectado=false;
		}
		super.onDestroy();
	}

	/**
	 * Funci�n para guardar la localizaci�n actual en la base de datos.
	 * @param loc localizaci�n a guardar.
	 */
	 private void setCurrentLocation(Location loc) {
		    
		   
	    	localizacion = loc;
	    	Log.i("gps","cambio de posicion long= "+loc.getLongitude()+" lati= "+loc.getLatitude()+" fecha:" + new Date(loc.getTime()).toLocaleString() + "nos lo envia "+loc.getProvider());
	    	FachadaCoordenadas.getInstance(this).insertCoordenadas(loc.getLongitude(), loc.getLatitude(), loc.getTime());
	    }
	
	 /**
	  * Clase que implemente a LocationListener para realizar las acciones necesarias cuando se 
	  * obtiene una nueva posici�n.
	  * @author David Herrero de la Pe�a
	  * @author Jonatan Santos Barrios
	  * @see LocationListener
	  *
	  */
	private class MiLocationListenerGPS implements LocationListener 
    {
        @Override
        public void onLocationChanged(Location loc) {
        	miLocationGPS = loc;
        	
            if (loc != null) {
                 
            	if(miLocation3G!=null){//ya hay una de 3g
            		if(miLocation3G.getAccuracy()<loc.getAccuracy()){//si la precisi�n de 3g es menos que de gps
            			setCurrentLocation(loc);
            		}
            		
            	}else{//no hay ninguna de 3g, guardamos la del gps
            		setCurrentLocation(loc);
            	}
                
               
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
	 /**
	  * Clase que implemente a LocationListener para realizar las acciones necesarias cuando se 
	  * obtiene una nueva posici�n.
	  * @author David Herrero de la Pe�a
	  * @author Jonatan Santos Barrios
	  * @see LocationListener
	  *
	  */
	private class MiLocationListener3G implements LocationListener 
   {
       @Override
       public void onLocationChanged(Location loc) {
    	   miLocation3G = loc;
       	
           if (loc != null) {
                
           	if(miLocationGPS!=null){//ya hay una de gps
           		if(miLocationGPS.getAccuracy()<loc.getAccuracy()){//si la precisi�n de gps es menos que de 3g
           			setCurrentLocation(loc);
           		}
           		
           	}else{//no hay ninguna de gps, guardamos la del 3g
           		setCurrentLocation(loc);
           	}
               
              
           }
       }

       @Override
       public void onProviderDisabled(String provider) {
           // TODO Auto-generated method stub
       }

       @Override
       public void onProviderEnabled(String provider) {
           // TODO Auto-generated method stub
       }

       @Override
       public void onStatusChanged(String provider, int status, 
           Bundle extras) {
           // TODO Auto-generated method stub
       }
   }
	
}
