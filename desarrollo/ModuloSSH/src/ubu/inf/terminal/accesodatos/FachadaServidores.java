package ubu.inf.terminal.accesodatos;

import java.util.ArrayList;

import ubu.inf.terminal.modelo.Servidor;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Clase que utiliza la aplicaci�n para acceder a la BD de forma simplificada, contiene m�todos para modificar la BD de forma transparente. Usa patron de dise�o Fachada y Singleton.
 * @author   David Herrero de la Pe�a
 * @author   Jonatan Santos Barrios
 * @version   1.0
 * @see  ServidoresSQLiteHelper
 */
public class FachadaServidores {
	Context context;
	/**
	 * @uml.property  name="helperservidores"
	 * @uml.associationEnd  
	 */
	ServidoresSQLiteHelper Helperservidores ;
	SQLiteDatabase DBservidores;
	/**
	 * @uml.property  name="myFachada"
	 * @uml.associationEnd  
	 */
	private static FachadaServidores myFachada;
	

	/**
	 * Contructor privado de la clase.
	 * @param context
	 */
	private FachadaServidores(Context context){
		this.context=context;
		Log.i("mssh", "ya tenemos la fachada, ahora a por el helper");
		Helperservidores = new ServidoresSQLiteHelper(context,"DBservidores", null, 1);
		Log.i("mssh", "helper creado correctamente");
	
	}
	/**
	 * M�todo est�tico para obtener la �nica instacia de la fachada.
	 * @param context
	 * @return referencia a la fachada �nica.
	 */
	public static FachadaServidores getInstance(Context context){
		
		Log.i("mssh", "entramos en getInstance");
		if(myFachada==null){
			Log.i("mssh","my fachada vale null");
			myFachada = new FachadaServidores(context);
		}
		return myFachada;
	}
	/**
	 * Funci�n para cerrar la fachada y la base de datos.
	 */
	public void closeFachada(){
		Log.i("mssh", "closeFachada,cerramos el helper");
		Helperservidores.close();
		myFachada=null;
	}
	/**
	 * Funci�n para obtener todos los Servidores guardados en la base de datos.
	 * @return ArrayList con todos los servidores de la base de datos.
	 */
	public ArrayList<Servidor> loadServidores(){
		Log.i("mssh", "loadServidores,creamos el array");
		ArrayList<Servidor> lista = new ArrayList<Servidor>();
		Log.i("mssh", "pedimos al helper la base de datos para leer");
		DBservidores = Helperservidores.getReadableDatabase();
		
		Cursor c = DBservidores.rawQuery("SELECT * FROM servidores", null);
		if(c.moveToFirst()){
			Log.i("mssh", "loadServidores,hay datos");
			do {
		          int id = c.getInt(0);
		          String host = c.getString(1);
		          String port = c.getString(2);
		          String user = c.getString(3);
		          String pass = c.getString(4);
		          String descripcion = c.getString(5);
		          Servidor aux = new Servidor(id, host, user, pass, port, descripcion);
		          lista.add(aux);
		         
		     } while(c.moveToNext());
			
		}else{
			Log.i("mssh", "loadServidores,no hay datos en la base de datos");
		}
		
		Log.i("mssh", "cerramos la base de datos");
		DBservidores.close();
		return lista;
	}
	/**
	 * Funci�n para a�adir un nuevo Servidor a la base de datos.
	 * @param ant ArrayList en el que se va a introducir tambi�n el Servidor nuevo.
	 * @param serv Servidor a introducir.
	 */
	public void insertServidor(ArrayList<Servidor> ant, Servidor serv){
		int id=0;
		String ip = serv.getIp();
		String port = serv.getPuerto();
		String user = serv.getUsuario();
		String pass = serv.getContrase�a();
		String desc = serv.getDescripcion();
		
		 Log.i("mssh", "fachada,insert,conseguimos la bd en forma W");
		DBservidores = Helperservidores.getWritableDatabase();
		if(DBservidores!=null){
			 Log.i("mssh", "hemos conseguido la base de datos");
			 try{
			 DBservidores.execSQL("INSERT INTO servidores(id, host, port, user, pass, descripcion) "+
			 "VALUES (NULL, '"+ip+"', '"+port+"', '"+user+"', '"+pass+"', '"+desc+"')");
			 Cursor c = DBservidores.rawQuery("SELECT last_insert_rowid();", null);
			 if(c.moveToFirst()){
				   id = c.getInt(0);
			}
			 serv.setId(id);
			 ant.add(serv);
			 }catch (SQLException e) {
				 Log.e("mssh", "error al introducir,clave duplicada? id = "+id );
			}
			 Log.i("mssh", "derramos la base de datos");
			 DBservidores.close();
			 
			
		}else{
		 Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
		
		
	}
	/**
	 * Borra el servidor de la base de datos dado el identificador.
	 * @param ant ArrayList de donte tambi�n se va a borrar si se puede.
	 * @param id identificador del servidor a borrar.
	 */
	public void deleteServidor(ArrayList<Servidor> ant,int id){
		 Log.i("mssh", "vamos a borrar");
		String sql = "DELETE FROM servidores WHERE id="+id;
		DBservidores = Helperservidores.getWritableDatabase();
		if(DBservidores!=null){
			 Log.i("mssh", "hemos conseguido la base de datos");
			 try{
			 DBservidores.execSQL(sql);
			
			 for(int i=0;i<ant.size();++i){
				 if(ant.get(i).getId()==id){
					 ant.remove(i);
					 break;
				 }
			 }
			 
			 }catch (SQLException e) {
				 Log.e("mssh", "error al borrar,clave incorrecta? id = "+id );
			}
			 Log.i("mssh", "cerramos la base de datos");
			 DBservidores.close();			
		}else{
		 Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
	}

	/**
	 * Funci�n para editar los datos de un Servidor almacenado en la base de datos.
	 * @param ant
	 * @param serv
	 */
	public void editServidor(ArrayList<Servidor> ant, Servidor serv) {
		int id = serv.getId();
		String ip = serv.getIp();
		String port = serv.getPuerto();
		String user = serv.getUsuario();
		String pass = serv.getContrase�a();
		String desc = serv.getDescripcion();
		
		
		String sql = "UPDATE servidores SET host='"+ip+"' ,port='"+port+"' ,user='"+user+"' ,pass='"+pass+"' ,descripcion='"+desc+"' WHERE id="+id+";";
		
		DBservidores = Helperservidores.getWritableDatabase();
		if(DBservidores!=null){
			 Log.i("mssh", "hemos conseguido la base de datos");
			 try{
			 DBservidores.execSQL(sql);
			
			 for(int i=0;i<ant.size();++i){
				 if(ant.get(i).getId()==id){
					 ant.get(i).setContrase�a(pass);
					 ant.get(i).setDescripcion(desc);
					 ant.get(i).setIp(ip);
					 ant.get(i).setPuerto(port);
					 ant.get(i).setUsuario(user);
					 break;
				 }
			 }
			 
			 }catch (SQLException e) {
				 Log.e("mssh", "error al update,clave incorrecta? id = "+id );
			}
			 Log.i("mssh", "cerramos la base de datos");
			 DBservidores.close();			
		}else{
		 Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
		
	}
	/**
	 * Funci�n para borrar las tablas y volver a crearlas de nuevo .
	 */
	public void borraTabla(){
		String sql = "DROP TABLE IF EXISTS servidores";
		String sqlCreate = "CREATE TABLE servidores (id INTEGER PRIMARY KEY,host TEXT,port TEXT,user TEXT,pass TEXT,descripcion TEXT)";
		DBservidores = Helperservidores.getWritableDatabase();
		if(DBservidores!=null){
			 Log.i("mssh", "hemos conseguido la base de datos");
			 try{
			 DBservidores.execSQL(sql);
			 Log.i("mssh", "hemos borrado, ahora a crearla" );
			 DBservidores.execSQL(sqlCreate);
			 Log.i("mssh", "se ha creado de nuevo la tabla" );
						 
			 }catch (SQLException e) {
				 Log.e("mssh", "error al borrar la tabla" );
			}
			 Log.i("mssh", "cerramos la base de datos");
			 DBservidores.close();			
		}else{
		 Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
		myFachada=null;
		Helperservidores = new ServidoresSQLiteHelper(context,"DBservidores", null, 1);
	}
}

