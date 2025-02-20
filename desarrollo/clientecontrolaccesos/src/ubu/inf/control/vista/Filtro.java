package ubu.inf.control.vista;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

import ubu.inf.control.R;
import ubu.inf.control.modelo.Notificacion;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Clase que sirve para crear el filtro para las notificaciones.
 * @author     David Herrero de la Pe�a
 * @author     Jonatan Santos Barrios
 * @version     1.0
 * @see PestanaMainNotificaciones
 * @uml.dependency   supplier="ubu.inf.control.vista.PestanaMainNotificaciones"
 */
public class Filtro extends Activity {

	/**
	 * Listener para el bot�n Cancelar.
	 * 
	 * @author David Herrero de la Pe�a.
	 * @author Jonatan Santos Barrios.
	 * 
	 */
	private class listenerCancelar implements View.OnClickListener {

		public void onClick(View arg0) {
			Intent resultData = new Intent();
			setResult(Activity.RESULT_CANCELED, resultData);
			finish();
		}
	}
	/**
	 * Listener para el bot�n OK.
	 * 
	 * @author David Herrero de la Pe�a.
	 * @author Jonatan Santos Barrios.
	 * 
	 */
	private class listenerOK implements View.OnClickListener {

		public void onClick(View arg0) {
			Intent resultData = new Intent();
			if (!(id.getText().toString().equals(""))) {// hay que tener en
														// cuenta el ID

				resultData.putExtra("id",
						Integer.parseInt(id.getText().toString()));
			} else {
				resultData.putExtra("id", -1);
			}

			resultData.putExtra("tipo", tipo.getSelectedItemPosition());// si es
																		// 0 es
																		// ninguna

			if (desde.isChecked()) {// hay que tener en cuenta desde
				resultData.putExtra("desde", datedesde.getTime());
			} else {
				resultData.putExtra("desde", -1L);
			}
			if (hasta.isChecked()) {// hay que tener en cuenta hasta
				resultData.putExtra("hasta", datehasta.getTime());
			} else {
				resultData.putExtra("hasta", -1L);
			}
			resultData.putExtra("urgencia", urgencia.getSelectedItemPosition());// 0
																				// ninguna

			setResult(Activity.RESULT_OK, resultData);
			guardaDatos();
			finish();
		}

	}
	private TreeSet<Notificacion> borrados;
	/**
	 * ID date dialog desde.
	 */
	private static final int DATE_DIALOG_ID_01 = 1;
	/**
	 * ID date dialog hasta.
	 */
	private static final int DATE_DIALOG_ID_02 = 2;

	/**
	 * ID time dialog desde.
	 */
	private static final int TIME_DIALOG_ID_03 = 3;
	/**
	 * ID time dialog hasta.
	 */
	private static final int TIME_DIALOG_ID_04 = 4;
	/**
	 * Spinner para elegir tipo, email o ssh.
	 */
	private Spinner tipo;
	/**
	 * Spinner para elegir urgencia, grave media o info.
	 */
	private Spinner urgencia;
	/**
	 * CheckBox para activar filtrado fecha desde.
	 */
	private CheckBox desde;
	/**
	 * CheckBox para activar filtrado fecha hasta.
	 */
	private CheckBox hasta;
	/**
	 * ID por el que filtrar.
	 */
	private EditText id;
	/**
	 * Fecha desde la que filtar.
	 */
	private CheckedTextView desde_fecha;
	/**
	 * Hora desde el que filtar.
	 */
	private CheckedTextView desde_hora;
	/**
	 * Fecha hasta la que filtar.
	 */
	private CheckedTextView hasta_fecha;
	/**
	 * Hora hasta la que filtar.
	 */
	private CheckedTextView hasta_hora;

	/**
	 * Boton OK.
	 */
	private Button OK;
	/**
	 * Boton cancelar.
	 */
	private Button Cancelar;
	private Date datedesde;
	private Date datehasta;
	// conjunto de variables para guardar la fecha desde la que filtrar.
	private int mDay_desde;
	private int mMes_desde;
	private int mYear_desde;
	private int mHora_desde;
	private int mMinuto_desde;
	// conjunto de variables para guardar la fehca hasta la que filtrar.
	private int mDay_hasta;
	private int mMes_hasta;
	private int mYear_hasta;

	private int mHora_hasta;

	private int mMinuto_hasta;
	// toma la fecha del dataPicker para el textView 01
	private DatePickerDialog.OnDateSetListener mDateSetListener_01 = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mYear_desde = year - 1900;
			mMes_desde = monthOfYear;
			mDay_desde = dayOfMonth;

			datedesde.setYear(mYear_desde);
			datedesde.setMonth(mMes_desde);
			datedesde.setDate(mDay_desde);
			updateDisplay();

		}

	};
	// toma la fecha del dataPicker para el textView 02
	private DatePickerDialog.OnDateSetListener mDateSetListener_02 = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mYear_hasta = year - 1900;
			mMes_hasta = monthOfYear;
			mDay_hasta = dayOfMonth;

			datehasta.setYear(mYear_hasta);
			datehasta.setMonth(mMes_hasta);
			datehasta.setDate(mDay_hasta);
			updateDisplay();

		}

	};

	// toma la fecha del timePicker para el textView 03
	private TimePickerDialog.OnTimeSetListener mTimeSetListener3 = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			mHora_desde = hour;
			mMinuto_desde = minute;

			datedesde.setHours(mHora_desde);
			datedesde.setMinutes(mMinuto_desde);
			updateDisplay();

		}
	};

	// toma la fecha del timePicker para el textView 04
	private TimePickerDialog.OnTimeSetListener mTimeSetListener4 = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			mHora_hasta = hour;
			mMinuto_hasta = minute;
			datehasta.setHours(mHora_hasta);
			datehasta.setMinutes(mMinuto_hasta);
			updateDisplay();

		}
	};

	private void cargarDatos() {
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

		int id = prefs.getInt("id", 1);// filtro de ID
		if (id != -1) {
			this.id.setText("" + id);
		}
		int tipo = prefs.getInt("tipo", 0);// filtro de tipo , 0 ninguna
	
		if (tipo != -1) {
			this.tipo.setSelection(tipo);
		}
		Date dia = new Date(prefs.getLong("desde", -1L));// filtro dia desde
		if (dia.getTime() != -1L) {// si hay fecha desde
			datedesde = dia;
		} else {
			datedesde = new Date();

		}
		dia = new Date(prefs.getLong("hasta", -1L));// filtro dia hasta

		if (dia.getTime() != -1L) {// si hay fecha hasta
			datehasta = dia;
		} else {
			datehasta = new Date();
		}
		int urgencia = prefs.getInt("urgencia", 1);// filtro de urgencia
		
		if (urgencia != -1) {

			this.urgencia.setSelection(urgencia);
		}
	}

	private void guardaDatos() {
		// guardamos los datos en el xml
		SharedPreferences preferencias = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferencias.edit();

		if (!(id.getText().toString().equals(""))) {// hay que tener en cuenta
													// el ID
			editor.putInt("id", Integer.parseInt(id.getText().toString()));

		} else {
			editor.putInt("id", -1);
		}

		editor.putInt("tipo", tipo.getSelectedItemPosition());// si es 0 es
																// ninguna

		if (desde.isChecked()) {// hay que tener en cuenta desde
			editor.putLong("desde", datedesde.getTime());
		} else {
			editor.putLong("desde", -1L);
		}
		if (hasta.isChecked()) {// hay que tener en cuenta hasta
			editor.putLong("hasta", datehasta.getTime());
		} else {
			editor.putLong("hasta", -1L);
		}
		editor.putInt("urgencia", urgencia.getSelectedItemPosition());// 0
																		// ninguna
		editor.commit();
	}

	/**
	 * Funci�n para obtener la referencia a todos los componentes y a�adir los
	 * datos iniciales.
	 */
	private void inicializa() {

		// obtengo la referencia a todos los componentes gr�fico.
		OK = (Button) findViewById(R.id.bt_filtro_OK);
		Cancelar = (Button) findViewById(R.id.bt_filtro_Cancel);
		tipo = (Spinner) findViewById(R.id.sp_filtro_tipo);
		urgencia = (Spinner) findViewById(R.id.sp_filtro_urgencia);
		desde = (CheckBox) findViewById(R.id.cb_filtro_desde);
		hasta = (CheckBox) findViewById(R.id.cb_filtro_hasta);
		id = (EditText) findViewById(R.id.et_filtro_id);
		desde_fecha = (CheckedTextView) findViewById(R.id.ctv_filtro_desde_fecha);
		desde_hora = (CheckedTextView) findViewById(R.id.ctv_filtro_desde_hora);
		hasta_fecha = (CheckedTextView) findViewById(R.id.ctv_filtro_hasta_fecha);
		hasta_hora = (CheckedTextView) findViewById(R.id.ctv_filtro_hasta_hora);
		// rellenamos los spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.tipo_filtro,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tipo.setAdapter(adapter);

		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.urgencia_filtro,
				android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		urgencia.setAdapter(adapter2);
		// cargamos los datos de las preferencias
		cargarDatos();

		// pongo la fecha

		mDay_desde = datedesde.getDate();
		mMes_desde = datedesde.getMonth();
		mYear_desde = datedesde.getYear();
		mHora_desde = datedesde.getHours();
		mMinuto_desde = datedesde.getMinutes();

		mDay_hasta = datehasta.getDate();
		mMes_hasta = datehasta.getMonth();
		mYear_hasta = datehasta.getYear();
		mHora_hasta = datehasta.getHours();
		mMinuto_hasta = datehasta.getMinutes();
		// a�ado el listenes al boton OK y Cancelar.
		OK.setOnClickListener(new listenerOK());
		Cancelar.setOnClickListener(new listenerCancelar());

		// evitamos que se clicken los textView
		desde_fecha.setEnabled(false);
		desde_hora.setEnabled(false);
		hasta_fecha.setEnabled(false);
		hasta_hora.setEnabled(false);
		// listener de los checkbox

		desde.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean estado) {
				if (estado) {
					desde_fecha.setEnabled(true);
					desde_hora.setEnabled(true);

				} else {
					desde_fecha.setEnabled(false);
					desde_hora.setEnabled(false);

				}
			}
		});
		hasta.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean estado) {
				if (estado) {
					hasta_fecha.setEnabled(true);
					hasta_hora.setEnabled(true);
				} else {
					hasta_fecha.setEnabled(false);
					hasta_hora.setEnabled(false);
				}

			}
		});
		// listener de los textview
		desde_fecha.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(DATE_DIALOG_ID_01);

			}
		});
		hasta_fecha.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(DATE_DIALOG_ID_02);

			}
		});
		desde_hora.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(TIME_DIALOG_ID_03);

			}
		});
		hasta_hora.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(TIME_DIALOG_ID_04);

			}
		});
		// actalizamos los datos a mostrar
		updateDisplay();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.filtro);

		inicializa();
	}

	/**
	 * Funci�n para crear el Dialog correspondiente dependiendo de que TextView
	 * pulsemos.
	 */
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID_01:
			return new DatePickerDialog(this, mDateSetListener_01,
					mYear_desde + 1900, mMes_desde, mDay_desde);
		case DATE_DIALOG_ID_02:
			return new DatePickerDialog(this, mDateSetListener_02,
					mYear_hasta + 1900, mMes_hasta, mDay_hasta);

		case TIME_DIALOG_ID_03:
			return new TimePickerDialog(this, mTimeSetListener3, mHora_desde,
					mMinuto_desde, true);

		case TIME_DIALOG_ID_04:
			return new TimePickerDialog(this, mTimeSetListener4, mHora_hasta,
					mMinuto_hasta, true);

		}
		return null;
	}

	/**
	 * Funci�n que se encarga de cambiar todos los datos representados.
	 */
	protected void updateDisplay() {
		desde_fecha.setText(mDay_desde + "/" + (mMes_desde + 1) + "/"
				+ (mYear_desde + 1900));
		hasta_fecha.setText(mDay_hasta + "/" + (mMes_hasta + 1) + "/"
				+ (mYear_hasta + 1900));
		String hora = "";
		if (mHora_desde < 10) {
			hora += "0" + mHora_desde;
		} else {
			hora += mHora_desde;
		}
		hora += ":";
		if (mMinuto_desde < 10) {
			hora += "0" + mMinuto_desde;
		} else {
			hora += mMinuto_desde;
		}
		desde_hora.setText(hora);
		hora = "";
		if (mHora_hasta < 10) {
			hora += "0" + mHora_hasta;
		} else {
			hora += mHora_hasta;
		}
		hora += ":";
		if (mMinuto_hasta < 10) {
			hora += "0" + mMinuto_hasta;
		} else {
			hora += mMinuto_hasta;
		}
		hasta_hora.setText(hora);

	}

}
