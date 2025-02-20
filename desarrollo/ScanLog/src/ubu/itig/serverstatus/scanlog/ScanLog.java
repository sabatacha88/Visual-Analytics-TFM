package ubu.itig.serverstatus.scanlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.daemon.*;


/**
 * Clase correspondiente al Daemon ScanLog.
 * @author      David Herrero de la Peña.
 * @author      Jonatan Santos Barrios.
 * @version      1.0
 * @uml.dependency   supplier="ubu.itig.serverstatus.scanlog.Analisis"
 * @uml.dependency   supplier="ubu.itig.serverstatus.scanlog.ComprobarSpamYAtaque"
 */
public class ScanLog implements Daemon{

	/**
	 * 
	 * Timer para controlar el tiempo.
	 * 
	 */
    private Timer timer = null;

	/**
	 * Conexión con la base de datos.
	 * @uml.property  name="conexion"
	 */
	private Connection conexion;
	
	/**
	 * 
	 * Nombre de la base de datos.
	 * 
	 */
	private String bd;
	
	/**
	 * 
	 * Nombre del usuario de la base de datos.
	 * 
	 */
	private String user;
	
	/**
	 * 
	 * Contraseña de usuario de la base de datos.
	 * 
	 */
	private String password;
	
	/**
	 * 
	 * Servidor donde se encuantra la base de datos.
	 * 
	 */
	private String server;
	
	/**
	 * 
	 * Cadena de conexión para la base de datos.
	 * 
	 */
	private String cadenaConexion;
	
	/**
	 * 
	 * Lector de buffer.
	 * 
	 */
	private BufferedReader bufferIn;
	
	/**
	 * 
	 * Servidor de socket.
	 * 
	 */
	private ServerSocket serverSocket;
	
	/**
	 * 
	 * Socket de escucha.
	 * 
	 */
	private Socket listenSocket;
	
	/**
	 * 
	 * Puerto de eschuca de los mensajes.
	 * 
	 */
	private int portIn;
	
	/**
	 * 
	 * Tiempo de comprobación de nuevos mensajes.
	 * 
	 */
	private int tiempoComprobacion;
	
	/**
	 * Numero de mails por minuto que se considera spam.
	 * @uml.property  name="numeroMails"
	 */
	private int numeroMails;
	
	/**
	 * Ultimo mensaje considerado como posible spam.
	 * @uml.property  name="mensajeSpam"
	 */
	private String mensajeSpam;
    
	/**
	 * Contador de mensaje considerados como posible spam.
	 * @uml.property  name="contadorSpam"
	 */
	private int contadorSpam;
	
	/**
	 * Numero de conexiones fallidas al correo por minuto que se considera ataque.
	 * @uml.property  name="numeroAtaque"
	 */
	private int numeroAtaque;
	
	/**
	 * Ultimo mensaje considerado como posible ataque.
	 * @uml.property  name="mensajeAtaque"
	 */
	private String mensajeAtaque;
    
	/**
	 * Contador de mensaje considerados como posible ataque.
	 * @uml.property  name="contadorAtaque"
	 */
	private int contadorAtaque;
    
    /**
     * Tareas a realizar al inicializar el demonio.
     * 
     * @param dc DaemonContext Contexto en el que se ejcutara el demonio.
     */
    @Override
    public void init(DaemonContext dc) {
    	 try{
    		 
    		 //Leer el fichero de propiedades
    		 lecturaFicheroPropiedades();
    		 
     		//Crear la conexion con base de datos
 	    	conexion = DriverManager.getConnection(cadenaConexion,user,password);
 	    	
 	    	//Crear el servidor socket y el socket de escucha
 	    	serverSocket = new ServerSocket(portIn);
 	    	listenSocket = serverSocket.accept();
 	    	
 	    	//Crear el lector de buffer
			bufferIn = new BufferedReader( new InputStreamReader(listenSocket.getInputStream()));
 	    	
 	    	System.out.println(new Date().toString() + " Iniciado el demonio");
 	    	
 	    } catch (IOException e){
 	    	System.err.println(new Date().toString() + " Excepción IO al iniciar el demonio: " + e.getMessage());
 	    } catch (SQLException e1) {
 	    	System.err.println(new Date().toString() + " Excepción SQL al iniciar el demonio: " + e1.getMessage());
		}
    }

	/**
     * Tareas a realizar al arrancar el demonio.
     * 
     */
	@Override
    public void start() {
		//Crear tarea repetitiva de comprobar spam
		crearTareaComprobarSpamYAtaque();
		
        //Crear la tarea repetitiva de analisis
        crearTareaAnalisis();
    }

    /**
     * Tareas a realizar al parar el demonio.
     * 
     */
    @Override
    public void stop() {
        try{
    	//Parar el reloj, y con ello la tarea repetitiva
    	if (timer != null) {
        	timer.cancel();
        }
    	//Cerrar buffer, sockets y conexion.
    	bufferIn.close();
        listenSocket.close();
        serverSocket.close();
        conexion.close();
        
        System.out.println(new Date().toString() + " Parado el demonio");
        } catch (SQLException e){
        	System.err.println(new Date().toString() + " Excepción SQL registrada: " + e.getMessage());
        } catch (IOException e1) {
        	System.err.println(new Date().toString() + " Excepción IO registrada: " + e1.getMessage());
		}
    }

    /**
     * Tareas a realizar al destruir el demonio.
     * 
     */
    @Override
    public void destroy() {
    	
    }
    
    /**
     * Leer el fichero de propiedades y obtiene los parametros de configuración
     * 
     */
    private void lecturaFicheroPropiedades() {
    	
    	Properties propiedades = new Properties();
		
    	try {
    		//Leemos el fichero
    	    propiedades.load(ScanLog.class.getClassLoader().getResourceAsStream("conf.properties"));
    	    
    	    //Obtenemos las propiedades
    	    server = propiedades.getProperty("dataBaseServer");
    	    bd = propiedades.getProperty("dataBaseName");
    	    user = propiedades.getProperty("dataBaseUser");
    	    password = propiedades.getProperty("dataBasePassword");
    	    
    	    portIn = Integer.parseInt(propiedades.getProperty("portIn"));
    	    tiempoComprobacion = Integer.parseInt(propiedades.getProperty("tiempoComprobacion"));
    	    numeroMails = Integer.parseInt(propiedades.getProperty("numeroMails"));
    	    numeroAtaque = Integer.parseInt(propiedades.getProperty("numeroAtaque"));
    	    
    	    cadenaConexion="jdbc:mysql://" + server + "/" + bd;
    	    
    	    System.out.println(new Date().toString() + " Leido archivo de configuración.");
    	    
    	} catch (IOException e) {
    		System.err.println(new Date().toString() + " Excepción IO al leer el archivo de configuración: " + e.getMessage());
    	}
    	
	}
    
    /**
	 * Crea una tarea repetitiva, de analisis.
	 * 
	 */
    private void crearTareaAnalisis() {
        //Obtener el tiempo
    	timer = new Timer();
    	//Configurar la tarea repetitiva
        timer.schedule(new Analisis(this), 0, tiempoComprobacion);
    }

    /**
	 * Crea una tarea repetitiva, de comprobar si es spam.
	 * 
	 */
    private void crearTareaComprobarSpamYAtaque() {
        //Obtener el tiempo
    	timer = new Timer();
    	//Configurar la tarea repetitiva
        timer.schedule(new ComprobarSpamYAtaque(this), 0, 60000);
    } 
    
    /**
	 * Devuelve la conexion de la base de datos.
	 * @return  Connection Conexion de la base de datos.
	 * @uml.property  name="conexion"
	 */
    public Connection getConexion(){
		return conexion;
	}
    
    /**
     * Devuelve el buffer de lectura.
     * 
     * @return BufferedReader Buffer de lectura.
     */
    public BufferedReader getBufferedReader(){
    	return bufferIn;
    }
    
    /**
	 * Devuelve el numero de mails por minutos que son considerados spam.
	 * @return  int Numero de mails/minuto.
	 * @uml.property  name="numeroMails"
	 */
    public int getNumeroMails(){
		return numeroMails;
	}
    
    /**
	 * Devuelve el numero de mails contados considerados spam en el ultimo minuto.
	 * @return  int Numero de mails contados.
	 * @uml.property  name="contadorSpam"
	 */
    public int getContadorSpam(){
		return contadorSpam;
	}
    
    /**
	 * Establece el numero de mails contados considerados spam en el ultimo minuto.
	 * @param numero  int Numero de mails contados.
	 * @uml.property  name="contadorSpam"
	 */
    public void setContadorSpam(int numero){
		contadorSpam = numero;
	}
    
    /**
	 * Devuelve el ultimo mensaje considerado spam.
	 * @return  String Mensaje spam.
	 * @uml.property  name="mensajeSpam"
	 */
    public String getMensajeSpam(){
		return mensajeSpam;
	}
    
    /**
	 * Establece el ultimo mensaje considerado spam.
	 * @param mensaje  String Mensaje spam.
	 * @uml.property  name="mensajeSpam"
	 */
    public void setMensajeSpam(String mensaje){
		mensajeSpam = mensaje;
	}
    
   /**
 * Devuelve el numero de conexiones fallidas por minutos que son considerados ataque.
 * @return  int Numero de mails/minuto.
 * @uml.property  name="numeroAtaque"
 */
   public int getNumeroAtaque(){
	return numeroAtaque;
}
   
   /**
 * Devuelve el numero de conexiones fallidas contadas considerados ataque en el ultimo minuto.
 * @return  int Numero de mails contados.
 * @uml.property  name="contadorAtaque"
 */
   public int getContadorAtaque(){
	return contadorAtaque;
}
   
   /**
 * Establece el numero de conexiones fallidas contados considerados ataque en el ultimo minuto.
 * @param numero  int Numero de mails contados.
 * @uml.property  name="contadorAtaque"
 */
   public void setContadorAtaque(int numero){
	contadorAtaque = numero;
}
   
   /**
 * Devuelve el ultimo mensaje considerado ataque.
 * @return  String Mensaje spam.
 * @uml.property  name="mensajeAtaque"
 */
   public String getMensajeAtaque(){
	return mensajeAtaque;
}
   
   /**
 * Establece el ultimo mensaje considerado ataque.
 * @param mensaje  String Mensaje spam.
 * @uml.property  name="mensajeAtaque"
 */
   public void setMensajeAtaque(String mensaje){
	mensajeAtaque = mensaje;
}
    
 }

/**
 * Clase correspondiente a la tarea repetitiva de Analizar el buffer.
 * @author  David Herrero de la Peña.
 * @author  Jonatan Santos Barrios.
 * @version  1.0
 */
class Analisis extends TimerTask {
	
	/**
	 * ScanLog que llama a la tarea repetitiva.
	 * @uml.property  name="scanLog"
	 * @uml.associationEnd  
	 */
	private ScanLog scanLog;


	/**
	 * Constructor de la tarea repetitiva.
	 * 
	 * @param conexion Conexion con la base de datos.
	 * @param is Lector de buffer de entrada.
	 */
	public Analisis(ScanLog log) {
		scanLog = log;
	}

	/**
	 * 
	 * Correr la tarea repetitiva
	 * 
	 */
	@Override
	public void run() {

		try {
			//Linea de entrada
			String inputLine;
			
			//Comprobar si hay lineas que leer
			while (scanLog.getBufferedReader().ready() && (inputLine = scanLog.getBufferedReader().readLine())!= "") {
				String linea;
				
				//Obtener la linea
				linea = inputLine.substring(inputLine.indexOf(">") + 1);

				//Analizar la linea
				analizarLinea(linea);
			}
		} catch (IOException e) {
			System.err.println(new Date().toString() + " Excepción IO registrada: " + e.getMessage());
		}
	}

	/**
	 * Analiza la linea para comprobar si es uno de los avisos que se deben notificar.
	 * 
	 * @param linea String con la linea del fichero.
	 */
	private void analizarLinea(String linea){

		String mensaje;
		Calendar fecha;
		int urgencia;

		//Obtenemos la fecha de la linea
		fecha = obtenerFecha(linea);

		//Obtener la parte del mensaje de la linea
		mensaje = linea.substring(16);
		mensaje = mensaje.substring(mensaje.indexOf(":") + 2);

		//Comprobar si es un mensaje de la conexión ssh o de email
		if(linea.indexOf("sshd") > -1){
			//Analizar mensaje ssh
			urgencia = obtenerUrgenciaSSH(mensaje);
			if (urgencia != -1){
				System.out.println(new Date().toString() + " Grabando notificacion: " + linea);
				grabarNotificacion(fecha, 0, urgencia, mensaje);
			}

		} else if (linea.indexOf("postfix") > -1) {
			//Analizar mensaje correo
			urgencia = obtenerUrgenciaCorreo(mensaje);
			if (urgencia != -1){
				System.out.println(new Date().toString() + " Grabando notificacion: " + linea);
				grabarNotificacion(fecha, 1, urgencia, mensaje);				
			}
		}		
	}

	/**
	 * Obtiene la urgencia del mensaje SSH.
	 * 
	 * @param mensaje String con el mensaje del aviso.
	 * @return int Tipo de mensaje: -1 Mensaje no notificable, 0 Intento de robo de claves, 1 Conexión fallida, 2 Conexión con exito.
	 */
	private int obtenerUrgenciaSSH(String mensaje){
		int tipo = -1;

		//Comprobamos si es uno de los mensajes a notificar
		if(mensaje.indexOf("Failed password") > -1){
			tipo = 1;
		} else if (mensaje.indexOf("POSSIBLE BREAK-IN ATTEMPT!") > -1){
			tipo = 0;
		} else if (mensaje.indexOf("Accepted password") > -1){
			tipo = 2;
		} else if (mensaje.indexOf("not receive identification string") > -1){
			tipo = 0;
		} else if (mensaje.indexOf("check pass; user unknown") > -1){
			tipo = 0;
		} else if (mensaje.indexOf("subsystem request for sftp") > -1){
			tipo = 2;
		}

		return tipo;
	}

	/**
	 * Obtiene la urgencia del mensaje de correo.
	 * 
	 * @param mensaje String con el mensaje del aviso.
	 * @return int Tipo de mensaje: -1 Mensaje no notificable, 0 Conexión con el servidor fallida, 2 Correo enviado con exito.
	 */
	private int obtenerUrgenciaCorreo(String mensaje){
		int tipo = -1;

		//Comprobamos si es uno de los mensajes a notificar
		if(mensaje.indexOf("SASL LOGIN authentication failed: authentication failure") > -1){
			scanLog.setContadorAtaque(scanLog.getContadorAtaque()+1);
			scanLog.setMensajeAtaque(mensaje);
			tipo = 1;
		} else if (mensaje.indexOf("status=sent") > -1){
			scanLog.setContadorSpam(scanLog.getContadorSpam()+1);
			scanLog.setMensajeSpam(mensaje);
			tipo = 1;
		}

		return tipo;
	}

	/**
	 * Obtiene la fecha y hora de la cadena de texto de la linea del fichero pasada.
	 * 
	 * @param lineaComprobar String con la linea del fichero.
	 * @return Calendar con la fecha y hora que se produjo la linea.
	 */
	private Calendar obtenerFecha(String lineaComprobar) {

		Calendar calendario = Calendar.getInstance();

		//Obtener los datos de la cadena
		String cadenaMes = lineaComprobar.substring(0,3);
		String cadenaDia = lineaComprobar.substring(4,6);
		String cadenaHora = lineaComprobar.substring(7,9);
		String cadenaMinuto = lineaComprobar.substring(10,12);
		String cadenaSegundo = lineaComprobar.substring(13,15);

		//Pasar los datos a integer
		int anno = calendario.get(Calendar.YEAR);
		int mes = obtenerMes(cadenaMes);
		int dia;
		//Comprobar si es un dia con un digito o dos
		if(Character.isDigit(cadenaDia.charAt(0))){
			dia = Integer.parseInt(cadenaDia);
		}else{
			String cadenaDiaAux = cadenaDia.substring(1,2);
			dia = Integer.parseInt(cadenaDiaAux);
		}
		int hora = Integer.parseInt(cadenaHora);
		int minuto = Integer.parseInt(cadenaMinuto);
		int segundo = Integer.parseInt(cadenaSegundo);

		//Establecer fecha y hora de la linea
		calendario.set(anno, mes, dia, hora, minuto, segundo);

		return calendario;
	}

	/**
	 * Transforma la abreviatura de un mes pasado en el integer correspondiente a dicho mes.
	 * 
	 * @param cadenaMes String Abreviatura del mes.
	 * @return int Con el numero de mes correspondiente a la cadena pasada.
	 */
	private int obtenerMes(String cadenaMes) {

		int mes = -1;

		//Comprobar de que mes se trata
		if(cadenaMes.equals("Jan")){
			mes = 0;
		} else if (cadenaMes.equals("Feb")){
			mes = 1;
		} else if (cadenaMes.equals("Mar")){
			mes = 2;
		} else if (cadenaMes.equals("Apr")){
			mes = 3;
		} else if (cadenaMes.equals("May")){
			mes = 4;
		} else if (cadenaMes.equals("Jun")){
			mes = 5;
		} else if (cadenaMes.equals("Jul")){
			mes = 6;
		} else if (cadenaMes.equals("Aug")){
			mes = 7;
		} else if (cadenaMes.equals("Sep")){
			mes = 8;
		} else if (cadenaMes.equals("Oct")){
			mes = 9;
		} else if (cadenaMes.equals("Nov")){
			mes = 10;
		} else if (cadenaMes.equals("Dec")){
			mes = 11;
		}

		return mes;
	}

	/**
	 * Graba en base de datos la información correspondiente de la notificación.
	 * 
	 * @param fecha Calendar Fecha y hora de la notificacion.
	 * @param tipoMensaje int Tipo de mensaje de la notificacion.
	 * @param urgencia int Urgencia de la notificacion.
	 * @param mensaje String Mensaje de la notificacion.
	 */
	private void grabarNotificacion(Calendar fecha, int tipoMensaje, int urgencia, String mensaje){
		//Pasar la fecha a fecha SQL
		java.sql.Timestamp horaSql = new java.sql.Timestamp(fecha.getTime().getTime());

		//Comprobamos el tipo de mensaje y decidimos si debe ser descarga o no la notificación
		int descargada = 0;
		if(tipoMensaje == 1){
			descargada = 1;
		}
		
		try {
			//Crear objetos Statement
			Statement st = scanLog.getConexion().createStatement();
			Statement st1 = scanLog.getConexion().createStatement();
			
			//Crear la notificacion
			String sql = "INSERT INTO Notificaciones (fecha, tipoMensaje, urgencia, mensaje) " +
					"VALUES ('" + horaSql + "', " + tipoMensaje +", " + urgencia + ", '" + mensaje + "');";

			st.executeUpdate(sql);

			//Obtener el id de la notificacion creada anteriormente
			sql = "SELECT MAX(idNotificacion) AS Notificacion FROM Notificaciones;";

			ResultSet rsIdNotificacion = st.executeQuery(sql);

			rsIdNotificacion.next();

			int idNotificacion = rsIdNotificacion.getInt("Notificacion");

			//Obtener los todos los dispositivos
			sql = "SELECT idDispositivo FROM Dispositivos;";

			ResultSet rsDispositivos = st.executeQuery(sql);

			//Recorrer los dispositivos
			while(rsDispositivos.next()){
				//Crear la relacion DispositivoNotificacion
				sql = "INSERT INTO DispositivosNotificaciones (idDispositivo, idNotificacion, descargada) " +
						" VALUES (" + rsDispositivos.getInt("idDispositivo") + ", " + idNotificacion + ", " + descargada + ");";

				st1.executeUpdate(sql);
			}
			//Cerrar los Statement
			st1.close();
			st.close();

		} catch (SQLException e) {
			System.err.println(new Date().toString() + " Excepción SQL registrada: " + e.getMessage());
		}
	}
}
	
	/**
	 * Clase correspondiente a la tarea repetitiva de comprobar si se considera spam.
	 * @author  David Herrero de la Peña.
	 * @author  Jonatan Santos Barrios.
	 * @version  1.0
	 */
	class ComprobarSpamYAtaque extends TimerTask {
		
		/**
		 * ScanLog que llama a la tarea repetitiva.
		 * @uml.property  name="scanLog"
		 * @uml.associationEnd  
		 */
		private ScanLog scanLog;

		/**
		 * Constructor de la tarea repetitiva.
		 * 
		 * @param conexion Conexion con la base de datos.
		 * @param numeroMails int Numero de mails por minuto que se consideran spam.
		 * @param contadorSpam int Numero de mails que se han enviado en el ultimo minuto.
		 * @param spam String con el ultimo mensaje de correo enviado, considerado como posible spam.
		 */
		public ComprobarSpamYAtaque(ScanLog log) {
			scanLog = log;
		}
		
		
		@Override
		public void run() {
			//Comprobamos si hay spam
			if(scanLog.getContadorSpam() >= scanLog.getNumeroMails()){
				System.out.println(new Date().toString() + " Grabando notificacion de spam: " + scanLog.getMensajeSpam());
				grabarNotificacion(Calendar.getInstance(), 1, 2, scanLog.getMensajeSpam());
			}
			
			//Comprobamos si hay ataque
			if(scanLog.getContadorSpam() >= scanLog.getNumeroMails()){
				System.out.println(new Date().toString() + " Grabando notificacion de ataque: " + scanLog.getMensajeAtaque());
				grabarNotificacion(Calendar.getInstance(), 1, 0, scanLog.getMensajeAtaque());
			}
			
			//Ponemos a 0 los contadores
			scanLog.setContadorSpam(0);
			scanLog.setContadorAtaque(0);
		}
		
		/**
		 * Graba en base de datos la información correspondiente de la notificación.
		 * 
		 * @param fecha Calendar Fecha y hora de la notificacion.
		 * @param tipoMensaje int Tipo de mensaje de la notificacion.
		 * @param urgencia int Urgencia de la notificacion.
		 * @param mensaje String Mensaje de la notificacion.
		 */
		private void grabarNotificacion(Calendar fecha, int tipoMensaje, int urgencia, String mensaje){
			//Pasar la fecha a fecha SQL
			java.sql.Timestamp horaSql = new java.sql.Timestamp(fecha.getTime().getTime());

			try {
				//Crear objetos Statement
				Statement st = scanLog.getConexion().createStatement();
				Statement st1 = scanLog.getConexion().createStatement();
				
				//Crear la notificacion
				String sql = "INSERT INTO Notificaciones (fecha, tipoMensaje, urgencia, mensaje) " +
						"VALUES ('" + horaSql + "', " + tipoMensaje +", " + urgencia + ", '" + mensaje + "');";

				st.executeUpdate(sql);

				//Obtener el id de la notificacion creada anteriormente
				sql = "SELECT MAX(idNotificacion) AS Notificacion FROM Notificaciones;";

				ResultSet rsIdNotificacion = st.executeQuery(sql);

				rsIdNotificacion.next();

				int idNotificacion = rsIdNotificacion.getInt("Notificacion");

				//Obtener los todos los dispositivos
				sql = "SELECT idDispositivo FROM Dispositivos;";

				ResultSet rsDispositivos = st.executeQuery(sql);

				//Recorrer los dispositivos
				while(rsDispositivos.next()){
					//Crear la relacion DispositivoNotificacion
					sql = "INSERT INTO DispositivosNotificaciones (idDispositivo, idNotificacion) " +
							" VALUES (" + rsDispositivos.getInt("idDispositivo") + ", " + idNotificacion + ");";

					st1.executeUpdate(sql);
				}
				//Cerrar los Statement
				st1.close();
				st.close();

			} catch (SQLException e) {
				System.err.println(new Date().toString() + " Excepción SQL registrada: " + e.getMessage());
			}
		}
	}
