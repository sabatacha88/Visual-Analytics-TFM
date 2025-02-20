package ubu.inf.gps.accesodatos;

import java.util.ArrayList;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 * Clase que utiliza la aplicaci�n para acceder a la BD de forma simplificada, contiene m�todos para modificar la BD de forma transparente. Usa patron de dise�o Fachada y Singleton.
 * @author  David Herrero de la Pe�a
 * @author  Jonatan Santos Barrios
 * @version  1.0
 * @see  ServidoresSQLiteHelper
 */
public class FachadaCoordenadas {
	/**
	 * Contexto desde el que se llama a la FachadaEmail.
	 */
	Context context;
	/**
	 * SQLite helper que se usa para crear la estructura b�sica de la BD.
	 * @uml.property  name="helpercoordenadas"
	 * @uml.associationEnd  
	 */
	CoordenadasSQLiteHelper Helpercoordenadas;
	/**
	 * Base de datos.
	 */
	SQLiteDatabase DBCoordenadas;
	/**
	 * Referencia a si misma.
	 * @uml.property  name="myFachada"
	 * @uml.associationEnd  
	 */
	private static FachadaCoordenadas myFachada;
	/**
	 * Constructor privado para crear la fachada, se crea una BD de nombre
	 * DBCoordenadas.
	 * 
	 * @param context
	 *            contexto desde el que se llama.
	 */
	private FachadaCoordenadas(Context context) {
		this.context = context;
		Log.i("gps",
				"ya tenemos la fachada de Coordenadas, ahora a por el helper");
		Helpercoordenadas = new CoordenadasSQLiteHelper(context, "DBCoordenadas", null,
				1);
		Log.i("gps", "helper coordenadas creado correctamente");

	}
	/**
	 * Funci�n que retorna la instancia �nica de de FachadaCoordenadas.
	 * 
	 * @param context
	 *            contexto desde la que se llama.
	 * @return la instancia de FachadaEmail si hab�a , o una nueva si no hab�a
	 *         una ya.
	 */
	public static FachadaCoordenadas getInstance(Context context) {

		Log.i("gps", "entramos en getInstance");
		if (myFachada == null) {
			Log.i("gps", "my fachada vale null");
			myFachada = new FachadaCoordenadas(context);
		}
		return myFachada;
	}
	/**
	 * Cierra el helper y pone la fachada a null.
	 */
	public void closeFachada() {
		Log.i("gps", "closeFachada,cerramos el helper");
		Helpercoordenadas.close();
		myFachada = null;
	}

	/**Funci�n que retorna todas las entradas de la base de datos, va metiendo todos los datos en 
	 * los distintos arrays que se le pasan como par�metros.
	 * 
	 * @param id array donde se colocar�n los id.
	 * @param longitud array donde se colocar�n las longitudes.
	 * @param latitud array donde se colocar�n las latitudes.
	 * @param fecha array donde se colocar�n las fechas , en segundos 
	 */
	public void loadCoordenadas(ArrayList<Integer> id,ArrayList<Double> longitud,ArrayList<Double> latitud,ArrayList<Long> fecha) {
		Log.i("gps", "loadCoordenadas,creamos el comando");
		

		
		Log.i("gps", "pedimos al helper la base de datos para leer");
		DBCoordenadas = Helpercoordenadas.getReadableDatabase();
		
		Cursor c = DBCoordenadas.rawQuery("SELECT * FROM coordenadas", null);
		if (c.moveToLast()) {
			Log.i("gps", "loadCoordenadas,hay datos");
			do {
				 id.add(c.getInt(0));
				 longitud.add(c.getDouble(1));
				 latitud.add(c.getDouble(2));
				 fecha.add(c.getLong(3));				
			} while (c.moveToPrevious());
			c.close();
		} else {
			Log.i("gps", "loadCoordenadas,no hay datos en la base de datos");
		}

		Log.i("gps", "cerramos la base de datos");
		DBCoordenadas.close();
		
	}
	/**Funci�n que retorna todas las �ltimas entradas de la base de datos, va metiendo todos los datos en 
	 * los distintos arrays que se le pasan como par�metros.
	 * 
	 * @param cantidad cantidad de datos a obtener.
	 * @param id array donde se colocar�n los id.
	 * @param longitud array donde se colocar�n las longitudes.
	 * @param latitud array donde se colocar�n las latitudes.
	 * @param fecha array donde se colocar�n las fechas , en segundos 
	 */
	public void loadCoordenadas(int cantidad,ArrayList<Integer> id,ArrayList<Double> longitud,ArrayList<Double> latitud,ArrayList<Long> fecha) {
		Log.i("gps", "loadCoordenadas,creamos el comando");
		int i =0;

		
		Log.i("gps", "pedimos al helper la base de datos para leer");
		DBCoordenadas = Helpercoordenadas.getReadableDatabase();
		
		Cursor c = DBCoordenadas.rawQuery("SELECT * FROM coordenadas", null);
		
		if (c.moveToLast()) {
			Log.i("gps", "loadCoordenadas,hay datos");
			do{
				 id.add(c.getInt(0));
				 longitud.add(c.getDouble(1));
				 latitud.add(c.getDouble(2));
				 fecha.add(c.getLong(3));	
				++i;
			}while(i<cantidad && c.moveToPrevious());
			c.close();
		} else {
			Log.i("gps", "loadCoordenadas,no hay datos en la base de datos");
		}

		Log.i("gps", "cerramos la base de datos");
		DBCoordenadas.close();
		
	}
	

	/**Inserta una nueva coordenada en la base de datos.
	 * 
	 * @param longitud longitud a introducir.
	 * @param latitud latitud a introducir.
	 * @param fecha fecha en segundos.
	 */
	public void insertCoordenadas(Double longitud,Double latitud ,Long fecha) {
		

		Log.i("gps", "fachada,insert,conseguimos la bd en forma W");
		DBCoordenadas = Helpercoordenadas.getWritableDatabase();
		if (DBCoordenadas != null) {
			Log.i("gps", "hemos conseguido la base de datos para a�adir");
			try {
				Log.i("gps", "ejecutamos el insert");
				DBCoordenadas
						.execSQL("INSERT INTO coordenadas(idCoordenada ,longitud ,latitud,fecha ) VALUES (NULL,"
								+ longitud + "," + latitud + ", "+fecha+")");
			} catch (SQLException e) {
				Log.e("gps", "error al introducir");
			}
			Log.i("gps", "cerramos la base de datos");
			DBCoordenadas.close();

		} else {
			Log.i("gps", "no hemos conseguido la base, retornamos null");
		}

	}
	/**
	 * Rertorna el numero de registros en la base de datos;
	 * 
	 * @return i cantidad de registros.
	 */
	public int getCantidad(){
		int i =0;
		String sql = "SELECT count(*) FROM coordenadas";
		DBCoordenadas = Helpercoordenadas.getReadableDatabase();
		
		Cursor c = DBCoordenadas.rawQuery(sql, null);
		if (c.moveToFirst()) {
			
			i=c.getInt(0);
			c.close();
		}
		return i;
		
	}

	/**Borra la fila de la base de datos que tenga ese mismo id.
	 * 
	 * @param id id de la coordenada a eliminar.
	 */
	public void deleteComando(int id) {
		Log.i("gps", "vamos a borrar");
		String sql = "DELETE FROM coordenadas WHERE idCoordenada=" + id;
		DBCoordenadas = Helpercoordenadas.getWritableDatabase();
		if (DBCoordenadas != null) {
			Log.i("gps", "hemos conseguido la base de datos");
			try {
				DBCoordenadas.execSQL(sql);

			} catch (SQLException e) {
				Log.e("gps", "error al borrar,clave incorrecta? id = " + id);
			}
			Log.i("gps", "cerramos la base de datos");
			DBCoordenadas.close();
		} else {
			Log.i("gps", "no hemos conseguido la base, retornamos null");
		}
	}

	
	/**
	 * Funci�n que borra la tabla de la BD y la vuelve a crear vac�a.
	 */
	public void borraTabla() {
		String sql = "DROP TABLE IF EXISTS coordenadas";
		 String sqlCreate1 = "CREATE TABLE coordenadas (idCoordenada INTEGER PRIMARY KEY,longitud REAL,latitud REAL,fecha INTEGER)";
		
		
		DBCoordenadas = Helpercoordenadas.getWritableDatabase();
		if (DBCoordenadas != null) {
			Log.i("gps", "hemos conseguido la base de datos");
			try {
				DBCoordenadas.execSQL(sql);
				
				Log.i("gps", "hemos borrado, ahora a crearla");
				DBCoordenadas.execSQL(sqlCreate1);
				
				Log.i("gps", "se ha creado de nuevo la tabla");

			} catch (SQLException e) {
				Log.e("gps", "error al borrar la tabla");
			}
			Log.i("gps", "cerramos la base de datos");
			DBCoordenadas.close();
		} else {
			Log.i("gps", "no hemos conseguido la base, retornamos null");
		}
		myFachada = null;
		Helpercoordenadas = new CoordenadasSQLiteHelper(context, "DBCoordenadas", null,
				1);
	}
}