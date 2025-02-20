package ubu.inf.control.vista;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import android.provider.Settings.Secure;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ubu.inf.control.R;
import ubu.inf.control.modelo.Servidor;
import ubu.inf.control.modelo.SingletonEmail;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Servicio que se encarga de comprobar si hay notificaciones relacionadas con
 * el Email en los distintos servidores que se han elegido desde la aplicaci�n.
 * 
 * @author David Herrero
 * @author Jonatan Santos
 * 
 * @version 1.0
 * @see Service
 */
public class ServicioEmail extends Service {

	/**
	 * id del dispositivo
	 */
	
	String id_dispositivo;
	
	/**
	 * Segundos que transcurren entre una llamada y la siguiente.
	 */
	private int segundos = 20;
	
	
	private NotificationManager mManager;
	/**
	 * Timer que se usa para que se efectuen las llamadas de forma peri�dica.
	 */
	private Timer timer = new Timer();
	private final IBinder mBinder = new MyBinder();
	/**
	 * NAMESPACE
	 */
	private String NAMESPACE = "http://notificador.serverstatus.itig.ubu";
	/**
	 * URL donde se encuentra el web service.
	 */
	private String URL = "";
	/**
	 * NAMESPACE+METHOD.
	 */
	private String SOAPACTION = "http://notificador.serverstatus.itig.ubu/hayNotificaciones";
	/**
	 * METHOD1,nombre del m�todo a usar para comprobar que hay notificaciones.
	 */
	private String METHOD = "hayNotificaciones";

	private String resultado;

	/**
	 * Metodo del hilo asincrono.
	 */
	private void ejecutar() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				for (int i = 0; i < SingletonEmail.getConexion().getHosts()
						.size(); ++i) {
					Log.i("control", "mmiramos el desde email "
							+ SingletonEmail.getConexion().getHosts()
									.get(i).getIp());
					conectaServidor(SingletonEmail.getConexion().getHosts()
							.get(i));

				}
			}
		}, 0, segundos * 1000);
	}

	/**
	 * Funci�n para notificar cuando ha ocurrido un error de comunicaci�n con el
	 * web service.
	 * @param s 
	 */
	private void NotificarError(Servidor s) {

		// Prepara la actividad que se abrira cuando el usuario pulse la
		// notificacion
		Intent intentNot = new Intent(this, Main.class);

		// Prepara la notificacion
		Notification notification = new Notification(R.drawable.ic_alert,
				"ERROR", System.currentTimeMillis());
		notification.setLatestEventInfo(this, "Servicio Email",
				"Error al conectarse al servidor "+s.getIp(), PendingIntent.getActivity(
						this.getBaseContext(), 0, intentNot,
						PendingIntent.FLAG_CANCEL_CURRENT));

		// Le a�ade sonido
		notification.defaults |= Notification.DEFAULT_SOUND;
		// Le a�ade vibraci�n
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		// Le a�ade luz mediante LED
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		// La notificaci�n se detendr� cuando el usuario pulse en ella
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// Lanza la notificaci�n
		mManager.notify(s.getId()* -1, notification);

	}

	/**
	 * Funci�n que se encarga de llamar a los distintos m�todos del webService.
	 * 
	 * @param s
	 *            Servidor al que consultar.
	 */
	private void conectaServidor(Servidor s) {
		resultado = "";

		// obtenemos el SoapObject
		SoapObject request = new SoapObject(NAMESPACE, METHOD);

		// objeto de propiedades
				PropertyInfo FahrenheitProp = new PropertyInfo();
				// nombre
				FahrenheitProp.setName("idDispositivo");
				// valor que
				FahrenheitProp.setValue(id_dispositivo);
				// tipo de valor
				FahrenheitProp.setType(String.class);
				request.addProperty(FahrenheitProp);
				
				PropertyInfo FahrenheitProp1 = new PropertyInfo();
				FahrenheitProp1.setName("tipoMensaje");
				// valor que
				FahrenheitProp1.setValue(1);
				// tipo de valor
				FahrenheitProp1.setType(Integer.class);

				// a�adimos las propiedades a la pregunta
				request.addProperty(FahrenheitProp1);
		// creamos el objeto http para conectarnos con el webservice
		 SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);

		envelope.dotNet = false;

		envelope.setOutputSoapObject(request);
		URL ="http://"+s.getIp()+":"+s.getPuerto()+"/axis2/services/Notificador.NotificadorHttpSoap12Endpoint/";
		//aqui pondr�amos la Ip del servidor
	 HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try

		{
			// hacer la primera petici�n para ver is hay notificaciones(con tipo y
			// dispositivo), si hay enviar la notificaci�n,
			// en el intent enviar cantidad y el servidor.
			androidHttpTransport.call(SOAPACTION, envelope);

			//utilizamos SoapPrimitive porque esperamos un valor simple
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			
			resultado = response.toString();
			if(!resultado.equals("0")){
				//TODO
				//SingletonServicios.getConexion().setCantidad(Integer.parseInt(resultado));
				Notificar(s,resultado);
				
			}
			Log.e("alarmChecker", "resultado Email: " + resultado);

			

		} catch (Exception e) {

			e.printStackTrace();
			NotificarError(s);

		}
	}
	
	/**
	 * Funci�n para notificar cuando se han encontrado nuevas notificaciones
	 * para descargar.
	 * @param s 
	 * @param resultado2 
	 */
	private void Notificar(Servidor s, String resultado2) {

		// Prepara la actividad que se abrira cuando el usuario pulse la
		// notificacion
		Intent intentNot = new Intent(this, PestanaMainNotificaciones.class);

		//ponemos como extra la cantidad , hacer Integer.parseInt(resultado2)
		intentNot.putExtra("cantidad",resultado2);
		// Prepara la notificacion
		Notification notification = new Notification(R.drawable.ic_email,
				resultado+" nuevos avisos", System.currentTimeMillis());
		notification.setLatestEventInfo(this, "Servicio Email",
				"Hay "+resultado2+" nuevos avisos de "+s.getIp(), PendingIntent.getActivity(
						this.getBaseContext(), 0, intentNot,
						PendingIntent.FLAG_CANCEL_CURRENT));

		// Le a�ade sonido
		notification.defaults |= Notification.DEFAULT_SOUND;
		// Le a�ade vibraci�n
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		// Le a�ade luz mediante LED
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		// La notificaci�n se detendr� cuando el usuario pulse en ella
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// Lanza la notificaci�n
		mManager.notify(s.getId(), notification);

	}

	@Override
	public void onCreate() {
		id_dispositivo= Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String aux = pref.getString("segundos", "10");
		segundos = Integer.parseInt(aux);
		Log.i("control", "servicio Email");
		
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		ejecutar();
	}

	@Override
	public void onDestroy() {
		Log.i("control", "onDestroy servicioEmail");
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
		
	}

	public class MyBinder extends Binder {
		ServicioEmail getService() {
			return ServicioEmail.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}