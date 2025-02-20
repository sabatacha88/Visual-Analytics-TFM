package ubu.inf.control.vista;

import ubu.inf.control.R;
import ubu.inf.control.R.layout;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.DigitalClock;
import android.widget.TabHost;

/**
 * Clase principal que contiene las dos pesta�as principales de la aplicaci�n.
 * @author           David Herrero
 * @author           Jonatan Santos
 * @version           1.0
 * @uml.dependency   supplier="ubu.inf.control.vista.PestanaMainFav"
 * @uml.dependency   supplier="ubu.inf.control.vista.PestanaMainEmail"
 * @uml.dependency   supplier="ubu.inf.control.vista.PestanaMainNotificaciones"
 */
public class Main extends TabActivity {
	/**
	 * TabHost que se usar�.
	 */
	private TabHost tabHost;
	/**
	 * Recursos de la aplicaci�n.
	 */
	private Resources res;
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //obtenemos la referencia al tabhost
        tabHost = getTabHost();
        //obtenemos la referencia a los recursos
        res = getResources(); 
       
        creaTabSSH();
        creaTabEmail();
        creaTabNotificaciones();
      
    }
    /**
     * Funci�n que crea la pesta�a de  servidores favoritos ssh, se encarga de crear el intent y de a�adirlo al tabhost.
     * @see PestanaMainSSH
     */
    private void creaTabSSH(){
  	  TabHost.TabSpec spec;
        Intent intent;
  	 //pesta�a favoritos
     //creamos el intent espec�fico para esa activity
      intent = new Intent().setClass(this, PestanaMainSSH.class);
      //creamos las especificaciones, nombre e icono
      spec = tabHost.newTabSpec("pestanamainfav").setIndicator("SSH", res.getDrawable(R.drawable.pestanamainfav));
      //a�adimos las especificaciones al intent
      spec.setContent(intent);
      //finalemte a�adirmos las especificaciones al tabhost para crear la pesta�a
      tabHost.addTab(spec);
  	 
  }
    /**
     * Funci�n que crea la pesta�a de servidores favoritos email, se encarga de crear el intent y de a�adirlo al tabhost.
     * @see PestanaMainSSH
     */
    private void creaTabEmail(){   	
    	  TabHost.TabSpec spec;
          Intent intent;
    	 //pesta�a favoritos
       //creamos el intent espec�fico para esa activity
        intent = new Intent().setClass(this, PestanaMainEmail.class);
        //creamos las especificaciones, nombre e icono
        spec = tabHost.newTabSpec("pestanamainemail").setIndicator("EMAIL", res.getDrawable(R.drawable.pestanamainemail));
        //a�adimos las especificaciones al intent
        spec.setContent(intent);
        //finalemte a�adirmos las especificaciones al tabhost para crear la pesta�a
        tabHost.addTab(spec);
    	 
    }
    /**
     * Funci�n que crea la pesta�a de notificaciones, se encarga de crear el intent y de a�adirlo al tabhost.
     * @see PestanaMainSSH
     */
  private void creaTabNotificaciones(){
  	TabHost.TabSpec spec;
      Intent intent;
	 //pesta�a manual
      //creamos el intent espec�fico para esa activity
    intent = new Intent().setClass(this, PestanaMainNotificaciones.class);
    //creamos las especificaciones, nombre e icono
    spec = tabHost.newTabSpec("pestanamainnot").setIndicator("notificaciones", res.getDrawable(R.drawable.pestanamainnot));
    //a�adimos las especificaciones al intent
    spec.setContent(intent);
    //finalemte a�adirmos las especificaciones al tabhost para crear la pesta�a
    tabHost.addTab(spec);
  }
  
  
}