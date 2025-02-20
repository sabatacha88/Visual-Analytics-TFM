package ubu.inf.terminal.modelo;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
/**
 * Clase para guardar todos los datos relativos a la conexi�n ssh. Patron singleton.
 * @author   David Herrero de la Pe�a
 * @author   Jonatan Santos Barrios
 * @version   1.0
 */
public class SingletonConexion {
	/**
	 * @uml.property  name="myConexion"
	 * @uml.associationEnd  
	 */
	private static SingletonConexion MyConexion;
	/**
	 * @uml.property  name="sesion"
	 */
	private Session sesion;
	/**
	 * @uml.property  name="jsch"
	 */
	private JSch jsch;

	/**
	 * Constructor privado.
	 */
	private SingletonConexion(){
		
	}
	/**
	 * Funci�n que retorna la �nica instancia de SingletonConexion.
	 * @return
	 */
	public static SingletonConexion getConexion(){
		if(MyConexion==null){
			MyConexion = new SingletonConexion();
		}
		return MyConexion;
	}
	/**
	 * @return
	 * @uml.property  name="sesion"
	 */
	public Session getSesion() {
		return sesion;
	}
	/**
	 * @param  sesion
	 * @uml.property  name="sesion"
	 */
	public void setSesion(Session sesion) {
		this.sesion = sesion;
	}
	/**
	 * @return
	 * @uml.property  name="jsch"
	 */
	public JSch getJsch() {
		return jsch;
	}
	/**
	 * @param  jsch
	 * @uml.property  name="jsch"
	 */
	public void setJsch(JSch jsch) {
		this.jsch = jsch;
	}
	
}
