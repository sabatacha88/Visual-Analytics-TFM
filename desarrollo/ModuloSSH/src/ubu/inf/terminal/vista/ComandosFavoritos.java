package ubu.inf.terminal.vista;

import java.util.ArrayList;

import ubu.inf.terminal.R;
import ubu.inf.terminal.accesodatos.FachadaComandos;
import ubu.inf.terminal.modelo.Script;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Actividad donde se muestran todos los comandos que han sido guardados en la base de datos.
 * @author David Herrero de la Pe�a
 * @author Jonatan Santos Barrios
 *
 */
public class ComandosFavoritos extends Activity {
	/**
	 * ArrayList con todos los scripts.
	 */
	private ArrayList<Script> comandos;
	/**
	 * Adapter para la lista.
	 */
	private ArrayAdapter<Script> adapter;
	/**
	 *Lista donde mostrar todos los scripts.
	 */
	private ListView list;
	
	private int pos;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comandos_favoritos);
		inicializa();

	}

	/**
	 * Inicia todos los componentes de la actividad.
	 */
	private void inicializa() {
		list = (ListView) findViewById(R.id.lv_comandos);
		list.setOnItemClickListener(new ListenerListViewComandos());
		registerForContextMenu(list);
		comandos = new ArrayList<Script>();
		comandos = FachadaComandos.getInstance(this).loadComandos();
		adapter = new ArrayAdapterComandos(this, comandos);
		list.setAdapter(adapter);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();


		menu.setHeaderTitle("Opciones");

		Log.i("mssh", "antes de inflar");
		inflater.inflate(R.menu.menu_comandos_favoritos, menu);
		Log.i("mssh", "despues");

	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.com_fav_borrar:
			borrar(info);

			return true;
		
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	/**
	 * Funci�n para borrar un script de la base de datos.
	 * @param info posici�n en el array del script a borrar.
	 */
	private void borrar(AdapterContextMenuInfo info) {
		// TODO Auto-generated method stub
		FachadaComandos.getInstance(ComandosFavoritos.this).deleteComando(comandos.get(info.position).getIdScript());
		comandos.remove(comandos.get(info.position));
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menufav, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		limpiar();
		return true;
	}
	/**
	 * Elimina todos los scripts de la base de datos.
	 */
	private void limpiar() {
		FachadaComandos.getInstance(this).borraTabla();
		comandos.clear();
		adapter.notifyDataSetChanged();

	}

	
	/**
	 * Clase que implementa el listener del la lista, para ejecutar
	 * una acci�n cuando se pulse en uno de los elementos de la misma.
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 *
	 *@see OnItemClickListener
	 */
	private class ListenerListViewComandos implements OnItemClickListener {
		/**
		 * Acci�n que se ejecuta al pulsar sobre un elemento de la lista.
		 */
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {
			pos=position;
			Log.i("mssh", "se ha hecho click");
			LayoutInflater factory = LayoutInflater.from(ComandosFavoritos.this);
        	final View selector = factory.inflate(R.layout.seleccionscript, null);
        	
        	EditText comando = (EditText) selector.findViewById(R.id.et_seleccionscript);
        	comando.setEnabled(false);
        	Script com = comandos.get(position);
        	String total="";
        	for(int i=0;i<com.getComandos().size();++i){
        		total+= com.getComandos().get(i)+"  "+'\n';
        		
        	}
        	     	
        	comando.setText(total);
        	comando.setTextColor(Color.WHITE);
        	
        	AlertDialog.Builder constructor =new AlertDialog.Builder(ComandosFavoritos.this);
        	constructor.setTitle("Script");
        	constructor.setView(selector);
        	constructor.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					Script c = comandos.get(pos);
					ArrayList<String> texto = c.getComandos();
					Intent resultData = new Intent();
					resultData.putExtra("comandos", texto);
					setResult(Activity.RESULT_OK, resultData);
					finish();
					
				}
			});
			
			
			
			constructor.create();
			constructor.show();

		}

	}
	/**
	 * Clase que implementa el listener del la lista, para ejecutar
	 * una acci�n cuando se pulse en uno de los elementos de la misma.
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 *
	 *@see OnItemClickListener
	 */
	private class ArrayAdapterComandos extends ArrayAdapter<Script> {
		private Activity context;
		private ArrayList<Script> datos;

		public ArrayAdapterComandos(Activity context, ArrayList<Script> array) {
			super(context, R.layout.list_comandos, array);
			// TODO Auto-generated constructor stub
			this.context = context;
			datos = comandos;
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = context.getLayoutInflater();
			View item = inflater.inflate(R.layout.list_comandos, null);

			final TextView nombre = (TextView) item
					.findViewById(R.id.tv_comandos_nombre2);
			String id = datos.get(position).getNombre();
			nombre.setText(id);

			TextView lblcantidad = (TextView) item
					.findViewById(R.id.tv_comandos_cantidad2);
			Integer cantidad = datos.get(position).getCantidad();
			lblcantidad.setText(cantidad.toString());

			TextView idComando = (TextView) item
					.findViewById(R.id.tv_list_comandos_id);
			Integer id1 = datos.get(position).getIdScript();
			idComando.setText(id1.toString());

			//aqui hay un problema
			
			
			Log.i("mssh", "creado un nuevo view");
			return (item);
			// TODO Auto-generated method stub
			// return super.getView(position, convertView, parent);
		}

	}
}
