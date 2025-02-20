package ubu.inf.control.vista;

import java.util.ArrayList;

import ubu.inf.control.R;
import ubu.inf.control.accesodatos.FachadaEmail;
import ubu.inf.control.accesodatos.FachadaSSH;
import ubu.inf.control.modelo.Servidor;
import ubu.inf.control.modelo.SingletonEmail;
import ubu.inf.control.modelo.SingletonSSH;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ToggleButton;

/**
 * Clase que implementa la funcionalidad de la pestana de favoritos.
 * @author          David Herrero
 * @author          Jonatan Santos
 * @version          1.0
 * @uml.dependency   supplier="ubu.inf.control.vista.ServicioSSH"
 * @uml.dependency   supplier="ubu.inf.control.vista.Formulario"
 * @uml.dependency   supplier="ubu.inf.control.vista.Preferencias"
 */
public class PestanaMainSSH extends Activity {
	private static final int REQUEST_FORMULARIO = 0;
	private static final int REQUEST_CONTEXT = 1;
	private int idaux;
	private ArrayList<Servidor> datos;

	private ToggleButton run;
	private ListView list;
	private ImageButton add;
	private TextView cantidad;
	/**
	 * @uml.property  name="fachada"
	 * @uml.associationEnd  
	 */
	private FachadaSSH fachada;

	/**
	 * @uml.property  name="adapter"
	 * @uml.associationEnd  
	 */
	private ArrayAdapterServidor adapter;

	private Boolean estabaactivo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pestanamainfav);
		Log.i("control", "onCreate ssh");
		inicializa();
	}

	@Override
	protected void onPause() {

		super.onPause();
		Log.i("control", "onPause ssh");
	}

	protected void onResume() {

		super.onPause();
		Log.i("control", "onResume ssh");
		cantidad.setText("servicios :"
				+ SingletonSSH.getConexion().getHosts().size());
	}

	/**
	 * Funci�n para obtener la referencia a todos los componentes y a�adir los
	 * datos iniciales.
	 */
	private void inicializa() {
		// obtenemos la fachada.
		fachada = FachadaSSH.getInstance(this);
		// creamos el array.
		datos = new ArrayList<Servidor>();
		datos = fachada.loadServidores();
		// obtenemos referencias a los componentes.
		cantidad = (TextView) findViewById(R.id.tv_pestanamainfav_contador);
		list = (ListView) findViewById(R.id.lv_main_fav_servidores);
		add = (ImageButton) findViewById(R.id.ib_main_fav_add);
		run = (ToggleButton) findViewById(R.id.tb_pestanamainfav_run);
		// a�adimos los listener.
		run.setOnCheckedChangeListener(new ListenerRun());
		add.setOnClickListener(new ListenerAdd());
		list.setOnItemClickListener(new ListenerListView());
		// registramos el ListView para que aparezca un ContextMenu.
		registerForContextMenu(list);
		// ponemos el adapter al ListView
		adapter = new ArrayAdapterServidor(this, datos);
		list.setAdapter(adapter);
		// miramos si el servicio SSH est� corriendo.
		if (isMyServiceRunning()) {
			run.setChecked(true);
		}
		// actualizmamos el textview.
		cantidad.setText("servicios :"
				+ SingletonSSH.getConexion().getHosts().size());

	}

	/**
	 * Listener del boton para arrancar el servicio.
	 * 
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 * 
	 */
	private class ListenerRun implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton boton, boolean estado) {
			if (estado && !isMyServiceRunning()) {// encendemos
			
				
				Intent myIntent = new Intent(PestanaMainSSH.this,
						ServicioSSH.class);
				
				startService(myIntent);
				
				Toast.makeText(PestanaMainSSH.this, "Alarma iniciada",
						Toast.LENGTH_SHORT).show();
				
				cantidad.setText("servicios :"
						+ SingletonSSH.getConexion().getHosts().size());

			} else if (!estado && isMyServiceRunning()) {// apagamos

				Intent myIntent = new Intent(PestanaMainSSH.this,
						ServicioSSH.class);
				stopService(myIntent);

				Log.i("control", "cancelamos la alarma");

				Toast.makeText(PestanaMainSSH.this, "Alarma detenida",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	/**
	 * Funci�n para eliminar todos los datos de la base de datos.
	 */
	private void limpiar() {
		fachada.borraTabla();
		datos.clear();
		adapter.notifyDataSetChanged();

	}

	/**
	 * Funci�n para editar uno de los Servidores.
	 * 
	 * @param info
	 */
	private void editar(AdapterContextMenuInfo info) {
		
		

		Intent i = new Intent(PestanaMainSSH.this, Formulario.class);

		i.putExtra("host", datos.get(info.position).getIp());
		i.putExtra("desc", datos.get(info.position).getDescripcion());
		i.putExtra("inicio", datos.get(info.position).isInicio());
		i.putExtra("color", datos.get(info.position).getColor());
		i.putExtra("puerto", datos.get(info.position).getPuerto());
		idaux = datos.get(info.position).getId();
		// quitamos de Singleton la IP , por si luego cambia
		estabaactivo = SingletonSSH.getConexion().getHosts()
				.remove(datos.get(info.position));
		PestanaMainSSH.this.startActivityForResult(i, REQUEST_CONTEXT);

	}

	/**
	 * Funci�n para borrar un Servidor de la lista y de la BD.
	 * 
	 * @param info
	 */
	private void borrar(AdapterContextMenuInfo info) {

		SingletonSSH.getConexion().getHosts()
				.remove(datos.get(info.position));
		fachada.deleteServidor(datos, datos.get(info.position).getId());
		adapter.notifyDataSetChanged();
		cantidad.setText("servicios :"
				+ SingletonSSH.getConexion().getHosts().size());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();

		menu.setHeaderTitle("Opciones");

		Log.i("control", "antes de inflar");
		inflater.inflate(R.menu.menu_fav, menu);
		Log.i("control", "despues");

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.CtxLstFavBorrar:

			borrar(info);

			return true;
		case R.id.CtxLstFavEdit:

			editar(info);

			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menufav, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear:
			limpiar();
			break;
		case R.id.ajustes_fav:
			preferencias();
			break;

		default:
			break;
		}

		return true;
	}

	/**
	 * Funci�n para a�adir un servidor al Singleton para que el Servicio lo
	 * utilice.
	 * 
	 * @param s
	 *            servidor que queremos a�adir.
	 */
	private void iniciaServicio(Servidor s) {

		if (!SingletonSSH.getConexion().getHosts().contains(s)) {

			SingletonSSH.getConexion().getHosts().add(s);
			cantidad.setText("servicios :"
					+ SingletonSSH.getConexion().getHosts().size());
		}

	}

	/**
	 * Funci�n para eliminar un servidor al Singleton para que el Servicio no lo
	 * utilice.
	 * 
	 * @param s
	 *            servidor que queremos eliminar.
	 */
	private void paraServicio(Servidor s) {

		SingletonSSH.getConexion().getHosts().remove(s);
		cantidad.setText("servicios :"
				+ SingletonSSH.getConexion().getHosts().size());

	}

	/**
	 * Funci�n para saber si el ServicioEmail est� funcionando.
	 * 
	 * @return true si est� funcionando, false si no lo est� haciendo.
	 */
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("ubu.inf.control.vista.ServicioSSH".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Funci�n que llama a la ventana para establecer las preferencias de la
	 * aplicaci�n.
	 */
	private void preferencias() {
		Intent i = new Intent(this, Preferencias.class);
		startActivity(i);
	}

	/**
	 * Funci�n para tratar los datos que se obtienen tras crear una
	 * ActivityForResult.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_FORMULARIO) {
			if (resultCode == Activity.RESULT_OK) {

				Bundle bundle = data.getExtras();
				Servidor serv = new Servidor(bundle.getString("host"),
						bundle.getString("desc"), bundle.getBoolean("inicio"),
						0, bundle.getInt("color"),Integer.parseInt(bundle.getString("puerto")));
				fachada.insertServidor(datos, serv);
				adapter.notifyDataSetChanged();
				Log.i("control", "hay nuevo servidor total =" + datos.size());

			}
		} else {
			if (requestCode == REQUEST_CONTEXT) {
				if (resultCode == Activity.RESULT_OK) {

					Log.i("control", "estamos en editar");
					Bundle bundle = data.getExtras();
					Servidor serv = new Servidor(bundle.getString("host"),
							bundle.getString("desc"),
							bundle.getBoolean("inicio"), idaux,
							bundle.getInt("color"),
							Integer.parseInt(bundle.getString("puerto")));
					// Log.i("control", "nuevo nombre =" + serv.getIp());

					fachada.editServidor(datos, serv);
					if (estabaactivo) {
						SingletonSSH.getConexion().getHosts().add(serv);
						estabaactivo = false;
					}
					adapter.notifyDataSetChanged();
					cantidad.setText("servicios :"
							+ SingletonSSH.getConexion().getHosts()
									.size());

				}
			}
		}
	}

	/**
	 * Listener del boton de a�adir.
	 * 
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 * 
	 */
	private class ListenerAdd implements View.OnClickListener {

		public void onClick(View arg0) {
			
			Intent i = new Intent(PestanaMainSSH.this, Formulario.class);
			PestanaMainSSH.this.startActivityForResult(i, REQUEST_FORMULARIO);

		}

	}

	/**
	 * Listener de la lista.
	 * 
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 * 
	 */
	private class ListenerListView implements OnItemClickListener {

		public void onItemClick(AdapterView<?> a, View v, int position, long id) {

			// TODO Por ahora no hace nada, se le podr�a a�adir funcionalidad.

		}
	}

	/**
	 * Adapter para la ListView.
	 * 
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 * 
	 * @see ArrayAdapter
	 * 
	 */
	private class ArrayAdapterServidor extends ArrayAdapter<Servidor> {
		private Activity context;
		private ArrayList<Servidor> datos;

		public ArrayAdapterServidor(Activity context, ArrayList<Servidor> array) {
			super(context, R.layout.list_servers, array);

			this.context = context;
			datos = array;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			LayoutInflater inflater = context.getLayoutInflater();
			View item = inflater.inflate(R.layout.list_servers, null);

			TextView ID = (TextView) item.findViewById(R.id.tv_listservers_id);
			Integer id = datos.get(position).getId();
			ID.setText(id.toString());
			ID.setBackgroundColor(datos.get(position).getColor());

			TextView ip = (TextView) item.findViewById(R.id.tv_listservers_ip2);
			ip.setText(datos.get(position).getIp()+":"+datos.get(position).getPuerto());
			TextView desc = (TextView) item
					.findViewById(R.id.tv_listservers_desc2);

			desc.setText(datos.get(position).getDescripcion());

			TextView estado = (TextView) item
					.findViewById(R.id.tv_listservers_estado);

			ToggleButton boton = (ToggleButton) item
					.findViewById(R.id.tb_listservers_servicio);
			boton.setBackgroundResource(R.drawable.tb_icon);

			ID.setBackgroundColor(datos.get(position).getColor());
			// ponemos el listener al ToggleButon
			boton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton boton1,
						boolean activado) {
					if (activado) {
						
						iniciaServicio(datos.get(position));
					} else {
						paraServicio(datos.get(position));
					}

				}
			});

			// para poner los botones en on si es necesario
			if (SingletonSSH.getConexion().getHosts()
					.contains(datos.get(position))) {
				Log.i("control", datos.get(position).getIp()
						+ " ya estaba ejecutandose, ponemos bot�n a ok");
				boton.setChecked(true);
			}
			if (datos.get(position).isInicio()) {
				estado.setTextColor(Color.GREEN);
				estado.setText("auto SI");

			} else {
				estado.setTextColor(Color.RED);
				estado.setText("auto NO");

			}
			return (item);

		}

	}

}
