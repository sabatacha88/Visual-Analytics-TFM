package ubu.inf.terminal.accesodatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**Clase que implementa el Helper para manejar la base de datos de forma transparente.
 * 
 * @author David Herrero de la Pe�a
 * @author Jonatan Santos Barrios
 * 
 * @version
 *
 */
public class ComandosSQLiteHelper extends SQLiteOpenHelper {

	
	private String sqlCreate1 = "CREATE TABLE scripts (idScript INTEGER PRIMARY KEY,nombre TEXT,cantidad INTEGER)";
	private String sqlCreate2 = "CREATE TABLE comandos (idComando INTEGER PRIMARY KEY, idScript INTEGER,comando TEXT,"
			+ "FOREIGN KEY (idScript) REFERENCES scripts ON UPDATE CASCADE ON DELETE CASCADE)";
//	private String trigger = "CREATE TRIGGER fk_idScript Before INSERT ON comandos "
//			+ "FOR EACH ROW BEGIN "
//			+ "SELECT CASE WHEN ((SELECT idScript FROM scripts"
//			+ "WHERE idScript = new.departamento ) IS NULL) "
//			+ "THEN RAISE (ABORT,'Foreign Key Violation') " + "END; " + "END ";

	/**
	 * Construcots de la clase.
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public ComandosSQLiteHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		if(!db.isReadOnly()){
		//activar foreign key
		db.execSQL("PRAGMA foreign_keys=ON;");
		}
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("mssh", "vamos a crear la tabla de comandos");
		// TODO Auto-generated method stub
		db.execSQL(sqlCreate1);
		db.execSQL(sqlCreate2);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS comandos");
		db.execSQL("DROP TABLE IF EXISTS scripts");

		// Se crea la nueva versi�n de la tabla
		db.execSQL(sqlCreate1);
		db.execSQL(sqlCreate2);
	}

}
