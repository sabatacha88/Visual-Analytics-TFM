package ubu.inf.control.accesodatos;

import java.util.ArrayList;

import ubu.inf.control.modelo.Servidor;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;

/**
 * Clase que utiliza la aplicaci�n para acceder a la BD de forma simplificada, contiene m�todos para modificar la BD de forma transparente. Usa patron de dise�o Fachada y Singleton.
 * @author  David Herrero de la Pe�a
 * @author  Jonatan Santos Barrios
 * @version  1.0
 * @see  ServidoresSQLiteHelper
 */
public class FachadaEmail {
	/**
	 * Contexto desde el que se llama a la FachadaEmail.
	 */
	Context context;
	/**
	 * SQLite helper que se usa para crear la estructura b�sica de la BD.
	 * @uml.property  name="helperEmail"
	 * @uml.associationEnd  
	 */
	ServidoresSQLiteHelper helperEmail;
	/**
	 * Base de datos.
	 */
	SQLiteDatabase DBservidores;
	/**
	 * Referencia a si misma.
	 * @uml.property  name="myFachada"
	 * @uml.associationEnd  
	 */
	private static FachadaEmail myFachada;

	
	/**
	 * Constructor privado para crear la fachada, se crea una BD de nombre
	 * DBservidores.
	 * 
	 * @param context
	 *            contexto desde el que se llama.
	 */
	private FachadaEmail(Context context) {
		this.context = context;

		helperEmail = new ServidoresSQLiteHelper(context, "DBservidores", null,
				1);

	}

	/**
	 * Funci�n que retorna la instancia �nica de de FachadaEmail.
	 * 
	 * @param context
	 *            contexto desde la que se llama.
	 * @return la instancia de FachadaEmail si hab�a , o una nueva si no hab�a
	 *         una ya.
	 */
	public static FachadaEmail getInstance(Context context) {
		// si la fachada es null
		if (myFachada == null) {

			myFachada = new FachadaEmail(context);
		}
		return myFachada;
	}

	/**
	 * Cierra el helper y pone la fachada a null.
	 */
	public void closeFachada() {

		helperEmail.close();
		myFachada = null;
	}

	/**
	 * Retorna todos los servidores que hubiera en la BD, si no hay ninguno
	 * retorna un ArrayList vac�o.
	 * 
	 * @return ArrayList con todos los Servidores
	 * 
	 */
	public ArrayList<Servidor> loadServidores() {

		ArrayList<Servidor> lista = new ArrayList<Servidor>();

		DBservidores = helperEmail.getReadableDatabase();

		Cursor c = DBservidores.rawQuery("SELECT * FROM email", null);
		if (c.moveToFirst()) {

			do {
				String ip = c.getString(1);

				String descripcion = c.getString(4);
				boolean inicio;
				if (c.getInt(2) != 0) {
					inicio = true;
				} else {
					inicio = false;
				}

				int id = c.getInt(0);
				int color = c.getInt(3);
				int puerto = c.getInt(5);
				Log.i("control", "puerto es :" +puerto);
				Servidor serv = new Servidor(ip, descripcion, inicio, id, color,puerto);
				lista.add(serv);

			} while (c.moveToNext());

		}

		DBservidores.close();
		return lista;
	}

	/**
	 * A�ade un Servidor a la base de datos.
	 * 
	 * @param ant
	 *            ArrayList en que se guardan toso los Servidores.
	 * @param serv
	 *            Servidor a introducir, el ID se a�ade luego.
	 */
	public void insertServidor(ArrayList<Servidor> ant, Servidor serv) {
		int id = 0;
		Log.i("control", "guardamos servidor con puerto : "+ serv.getPuerto());
		String ip = serv.getIp();
		String desc = serv.getDescripcion();
		int puerto = serv.getPuerto();
		int color = serv.getColor();
		boolean inicio = serv.isInicio();
		int aux;
		if (inicio) {
			aux = 1;
		} else {
			aux = 0;
		}

		DBservidores = helperEmail.getWritableDatabase();
		if (DBservidores != null) {

			try {
				DBservidores
						.execSQL("INSERT INTO email(id,host,inicio,color,descripcion,puerto) "
								+ "VALUES (NULL,'"
								+ ip
								+ "',"
								+ aux
								+ ","
								+ color + ",'" + desc + "',"+puerto+")");
				Cursor c = DBservidores.rawQuery("SELECT last_insert_rowid();",
						null);
				if (c.moveToFirst()) {
					id = c.getInt(0);
				}
				serv.setId(id);
				ant.add(serv);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			DBservidores.close();

		}

	}

	/**
	 * Borra una linea de la BD dado un id.
	 * 
	 * @param ant
	 *            ArrayList con los Servidores.
	 * @param id
	 *            ID de la linea a borrar.
	 */
	public void deleteServidor(ArrayList<Servidor> ant, int id) {

		String sql = "DELETE FROM email WHERE id=" + id;
		DBservidores = helperEmail.getWritableDatabase();
		if (DBservidores != null) {

			try {
				DBservidores.execSQL(sql);

				for (int i = 0; i < ant.size(); ++i) {
					if (ant.get(i).getId() == id) {
						ant.remove(i);
						break;
					}
				}

			} catch (SQLException e) {
				Log.e("mssh", "error al borrar,clave incorrecta? id = " + id);
			}

			DBservidores.close();
		} else {
			Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
	}

	/**
	 * Funci�n que edita una linea de la BD.
	 * 
	 * @param ant
	 *            ArrayList con los servidores.
	 * @param serv
	 *            servidor con los datos a editar.
	 */
	public void editServidor(ArrayList<Servidor> ant, Servidor serv) {
		int id = serv.getId();
		String ip = serv.getIp();
		String desc = serv.getDescripcion();
		int color = serv.getColor();
		int puerto = serv.getPuerto();
		boolean inicio = serv.isInicio();
		int aux;
		if (inicio) {
			aux = 1;
		} else {
			aux = 0;
		}

		String sql = "UPDATE email SET host='" + ip + "' ,color=" + color
				+ " ,inicio=" + aux + " ,descripcion='" + desc + "' ,puerto="+puerto+" WHERE id="
				+ id + ";";

		DBservidores = helperEmail.getWritableDatabase();
		if (DBservidores != null) {

			try {
				DBservidores.execSQL(sql);

				for (int i = 0; i < ant.size(); ++i) {
					if (ant.get(i).getId() == id) {

						ant.get(i).setDescripcion(desc);
						ant.get(i).setIp(ip);
						ant.get(i).setPuerto(puerto);
						ant.get(i).setColor(color);
						ant.get(i).setInicio(inicio);
						break;
					}
				}

			} catch (SQLException e) {
				Log.e("control", "error al update,clave incorrecta? id = " + id);
			}

			DBservidores.close();
		} else {
			Log.i("control", "no hemos conseguido la base, retornamos null");
		}

	}

	/**
	 * Funci�n que borra la tabla de la BD y la vuelve a crear vac�a.
	 */
	public void borraTabla() {
		String sql = "DROP TABLE IF EXISTS email";
		String sqlCreate = "CREATE TABLE email (id INTEGER PRIMARY KEY,host TEXT,inicio INTEGER,color INTEGER,descripcion TEXT,puerto INTEGER)";
		DBservidores = helperEmail.getWritableDatabase();
		if (DBservidores != null) {

			try {
				DBservidores.execSQL(sql);

				DBservidores.execSQL(sqlCreate);

			} catch (SQLException e) {
				Log.e("mssh", "error al borrar la tabla");
			}

			DBservidores.close();
		} else {
			Log.i("mssh", "no hemos conseguido la base, retornamos null");
		}
		myFachada = null;
		helperEmail = new ServidoresSQLiteHelper(context, "DBservidores", null,
				1);
	}
}
