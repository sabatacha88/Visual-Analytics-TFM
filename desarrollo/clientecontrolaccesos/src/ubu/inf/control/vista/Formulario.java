package ubu.inf.control.vista;


import ubu.inf.control.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
/**
 * Formulario para crear nuevos servidores o editar los que ya hab�a.
 * 
 * @author David Herrero de la Pe�a
 * @author Jonatan Santos Barrios
 * 
 * @version 1.0
 * 
 *@see PestanaMainSSH
 *@see PestanaMainEmail
 */
public class Formulario extends Activity {
	
	private EditText host;
	private EditText puerto;
	private CheckBox inicio;
	private SeekBar barracolor;
	private ImageView color;
	private int actual;
	private EditText desc;
	private Button OK;
	private Button cancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formulario);
		inicializa();
		//miramos si nos han enviado informaci�n
		if(this.getIntent().getExtras()!=null){
			Bundle aux = getIntent().getExtras();
			host.setText(aux.getString("host"));
			desc.setText(aux.getString("desc"));
			Integer a = aux.getInt("puerto");
			puerto.setText(a.toString());
			inicio.setChecked(aux.getBoolean("inicio"));
			actual = aux.getInt("color");
			color.setBackgroundColor(actual);
		}
		
	}

	/**
	 * Funci�n para iniciar todos los componentes.
	 */
	private void inicializa() {
		//referencias a todos los componentes
		desc=(EditText) findViewById(R.id.et_formulario_descverdadera);
		host=(EditText) findViewById(R.id.et_formulario_ip);
		puerto=(EditText) findViewById(R.id.et_formulario_desc);
		OK = (Button) findViewById(R.id.bt_formulario_ok);
		cancel = (Button) findViewById(R.id.bt_formulario_cancel);		
		inicio = (CheckBox) findViewById(R.id.cb_formulario_inicio);
		color = (ImageView) findViewById(R.id.iv_formulario_coloractual);
		barracolor = (SeekBar) findViewById(R.id.seekBar1);
		//ponemos color , m�ximo y m�mino a la barra de selecci�n y listener.
		color.setBackgroundColor(actual);
		actual=-1;		
		barracolor.setMax(Color.BLACK * -1);
		barracolor.setOnSeekBarChangeListener(new ListenerBarra());
		//listener a los botones
		OK.setOnClickListener(new listenerOK());
		cancel.setOnClickListener(new listenerCancelar());
		
		
	}
	/**
	 * Listener de la barra para elegir color.
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 *
	 */
	private class ListenerBarra implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			color.setBackgroundColor(progress * -1);
			actual=progress * -1;
			
		}

		

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
			
		}



		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
			
		}
		
	}
	/**
	 * Listener del bot�n OK.
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 *
	 */
	private class listenerOK implements View.OnClickListener {

		public void onClick(View arg0) {
			Intent resultData = new Intent();
			
			try{
				resultData.putExtra("host", host.getText().toString());
				resultData.putExtra("desc", desc.getText().toString());
				resultData.putExtra("inicio", inicio.isChecked());
				resultData.putExtra("color", actual);
				Integer.parseInt(puerto.getText().toString());
				resultData.putExtra("puerto", puerto.getText().toString());
		          
				setResult(Activity.RESULT_OK, resultData);
	            finish();
			}catch(Exception e){
				e.printStackTrace();
				Toast.makeText(Formulario.this, "El puerto deber ser un n�mero", Toast.LENGTH_SHORT).show();
			}
			
        } 
		
	}
	/**
	 * Listener del boton cancelar.
	 * @author David Herrero de la Pe�a
	 * @author Jonatan Santos Barrios
	 *
	 */
	private class listenerCancelar implements View.OnClickListener {

		public void onClick(View arg0) {
			Intent resultData = new Intent();
			setResult(Activity.RESULT_CANCELED, resultData);
            finish();
		}
	}

}
