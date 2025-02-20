package ubu.inf.gps.vista;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ubu.inf.gps.accesodatos.FachadaCoordenadas;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Servicio que se encarga de enviar un sms con las �ltimas posiciones de GPS guardadas en 
 * la base de datos cada x minutos.
 * 
 * @author David Herrero
 * @author Jonatan Santos
 * 
 * @version 1.0
 * @see Service
 */
public class ServicioEnvioSMS extends Service {
	/**
	 * id del dispositivo.
	 */
	String id_dispositivo;
	/**
	 * Tel�fono al que enviar el sms.
	 */
	String telefono;
	/**
	 * Minutos entre un sms y el siguiente.
	 */
	int minutos;
	
	Timer timer = new Timer();
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		id_dispositivo= Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
	    telefono = pref.getString("avisosmsnumero", "666666666");
		
		String aux1 = pref.getString("avisosmstiempo", "30");
		minutos = Integer.parseInt(aux1);
		Log.i("gps", "enviamos sms a "+telefono+" cada "+minutos+ " minutos");
		//FachadaCoordenadas.getInstance(this).insertCoordenadas(555.1, 555.4, 55);
		ejecutar();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
	}
	/**
	 * Funci�n que se encarga de enviar un sms con la �ltima posici�n de GPS conocida.
	 */
	private void ejecutar(){
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				ArrayList<Integer> ID =new ArrayList<Integer>();
				ArrayList<Double> longitud =new ArrayList<Double>();
				ArrayList<Double> latitud =new ArrayList<Double>();
				ArrayList<Long> fecha =new ArrayList<Long>();
				String mensaje = "";
				Date aux ;
				FachadaCoordenadas.getInstance(ServicioEnvioSMS.this).loadCoordenadas(1, ID, longitud, latitud, fecha);
				if(ID.size()>0){
				 mensaje = "";
				 aux=new Date(fecha.get(0));
				mensaje+="id:"+id_dispositivo;	
				
				mensaje+=" lat: "+latitud.get(0);	
				mensaje+=" long: "+longitud.get(0);	
				mensaje+=" fecha: "+aux.toLocaleString();	
				Log.i("gps", "enviamos el mensaje desde servicio:"+mensaje);
				sendSMS(telefono, mensaje);	
				}else{
					Log.i("gps", "envio sms , no hay datos");
					sendSMS(telefono, "No hay ninguna locacizacion,puede que no est� activado el GPS");	
				}
			}
		}, 0, minutos*60*1000);
	}
	
	private void sendSMS(String phoneNumber, String message)
    {        
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, ServicioEnvioSMS.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);        
    }    
}
