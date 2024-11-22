package servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO: Complementa esta clase para que acepte conexiones TCP con clientes para
 * recibir un boleto, generar la respuesta y finalizar la sesion
 */
public class ServidorTCP {
	// Creo dos atributos de tipo socket para conectar con el cliente
	private Socket socketCliente;
	private ServerSocket socketServidor;
	// Creo dos atributos de comunicacion de datos
	private BufferedReader entrada;
	private PrintWriter salida;
	private String[] respuesta;
	private int[] combinacion;
	private int reintegro;
	private int complementario;

	/**
	 * Constructor
	 */
	public ServidorTCP(int puerto) {

		this.socketCliente = null;
		this.socketServidor = null;
		this.entrada = null;
		this.salida = null;

		try {
			// creo un objeto socket se servidor con el puerto a que va a intentar
			// conectarse el cliente
			socketServidor = new ServerSocket(puerto);
			System.out.println("Esperando conexión...");
			// Asigno al atributo socketCliente lo que devuelva el intento de conexión
			socketCliente = socketServidor.accept();
			// Lo muestro por pantalla
			System.out.println("Conexión acceptada: " + socketCliente);
			// Asigno a los dos objetos de escritura y lectura un bufferedReader y un
			// printwriter respectivamente
			entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			salida = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketCliente.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("No puede escuchar en el puerto: " + puerto);
			System.exit(-1);
		}

		this.respuesta = new String[9];
		this.respuesta[0] = "Boleto inv�lido - N�meros repetidos";
		this.respuesta[1] = "Boleto inv�lido - n�meros incorretos (1-49)";
		this.respuesta[2] = "6 aciertos";
		this.respuesta[3] = "5 aciertos + complementario";
		this.respuesta[4] = "5 aciertos";
		this.respuesta[5] = "4 aciertos";
		this.respuesta[6] = "3 aciertos";
		this.respuesta[7] = "Reintegro";
		this.respuesta[8] = "Sin premio";
		generarCombinacion();
		imprimirCombinacion();
	}

	/**
	 * @return Debe leer la combinacion de numeros que le envia el cliente
	 */
	public String leerCombinacion() {
		String linea = "";
		try {
			// ASigno a variable string lo que envie el cliente con la cadena con los
			// numeros introducidos
			linea = entrada.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return linea;
	}

	/**
	 * @return Debe devolver una de las posibles respuestas configuradas
	 */
	public String comprobarBoleto(String linea) {

		String[] vecString = linea.split(" ");
		int[] numerosUsuario = new int[6];

		for (int i = 0; i < vecString.length; i++) {
			numerosUsuario[i] = Integer.parseInt(vecString[i]);
		}

		// Comprobar repetidos
		if (hayRepetidos(numerosUsuario)) {
			return this.respuesta[0];
		}

		// Comprobar numero fuera de rango
		if (hayFueraDeRango(numerosUsuario)) {
			return this.respuesta[1];
		}

		// Comprobar numero de aciertos
		int msgInicial = comprobarAciertos(numerosUsuario);

		// Comprobar reintegro
		if (hayReintegro(msgInicial, numerosUsuario)) {
			return this.respuesta[7];
		}

		// Comprobar complementario
		if (hayComplementario(msgInicial, numerosUsuario)) {
			return this.respuesta[3];
		}

		return this.respuesta[msgInicial];
	}

	/**
	 * Método que comprueba si han habido 5 aciertos y se cumple que el numero extra
	 * coincide con el complementario
	 * 
	 * @param msgInicial     = numero inicial de aciertos que habrá si no se tienen
	 *                       en cuenta las excepciones
	 * @param numerosUsuario = numeros que ha introducido el usuario
	 * @return True = si ha cumplido el complementario / False = no ha cumplido el
	 *         complementario
	 */
	private boolean hayComplementario(int msgInicial, int[] numerosUsuario) {
		// Miro si el mensaje es 4 que indica que han habido 5 aciertos
		if (msgInicial == 4) {
			// El primer boolean mira si el complementario esta entre todos los numeros
			// introducidos
			boolean complementarioMarcado = false;
			// El segundo booleano mira que no esté entre los numeros ganadores, de esa
			// forma garantiza que solo se puede coger si es el sobrante
			boolean complementarioCogido = false;
			for (int i = 0; i < numerosUsuario.length; i++) {
				if (numerosUsuario[i] == complementario) {
					complementarioMarcado = true;
				}
			}

			if (complementarioMarcado) {
				for (int i = 0; i < numerosUsuario.length; i++) {
					if (combinacion[i] == complementario) {
						complementarioCogido = true;
					}
				}
			}

			// Si se cumplen ambas condiciones devolverá true
			if (complementarioMarcado && !complementarioCogido) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Métood que comprueba si hay menos de 3 aciertos y si el reintegro coincide
	 * 
	 * @param msgInicial     = Msg sobre los aciertos que ha habido sin tener en
	 *                       cuenta las excepciones
	 * @param numerosUsuario = los numeros introducidos por e usuario
	 * @return True = si que se cumple el reintegro / False = no se cumple
	 */
	private boolean hayReintegro(int msgInicial, int[] numerosUsuario) {
		// Mira si hay 0 aciertos ( menos de 3)
		if (msgInicial == 8) {
			// Y comprueba si algun numero coincide con el reintegro
			for (int i = 0; i < numerosUsuario.length; i++) {
				if (numerosUsuario[i] == reintegro) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Método que mira si hay algun numero fuera del rango permitido
	 * 
	 * @param numerosUsuario = Numeros introducidos por el usuario
	 * @return True = Hay numeros fuera de rango
	 */
	private boolean hayFueraDeRango(int[] numerosUsuario) {
		for (int i = 0; i < numerosUsuario.length; i++) {
			if (numerosUsuario[i] > 49 || numerosUsuario[i] < 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Métoco qud comprueba si hay repetidos
	 * 
	 * @param numerosUsuario
	 * @return
	 */
	private boolean hayRepetidos(int[] numerosUsuario) {
		// Ordeno los numeros para que sea mas facil
		Arrays.sort(numerosUsuario);
		// Comoruebo si hay dos numeros iguales seguidos
		for (int i = 1; i < numerosUsuario.length; i++) {
			if (numerosUsuario[i] == numerosUsuario[i - 1]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Método que comprueba cuantos aciertos hay sin contar excepciones
	 * 
	 * @param numerosUsuario
	 * @return Respuesta que se deberá devolver en funcion de los aciertos
	 */
	private int comprobarAciertos(int[] numerosUsuario) {

		int contador = 0;

		// Cuento los aciertos con el boleto ganador
		for (int i = 0; i < numerosUsuario.length; i++) {
			for (int j = 0; j < numerosUsuario.length; j++) {
				if (numerosUsuario[i] == combinacion[j]) {
					contador++;
				}
			}
		}

		// Devuelvo 8 de haber menos de 3 aciertos
		if (contador < 3) {
			return 8;
		}

		// Con un seitch compruebo que respuesta devolver en funcion de cuantos aciertos
		// haya encontrado el contador
		switch (contador) {
		case 3:
			return 6;
		case 4:
			return 5;
		case 5:
			return 4;
		case 6:
			return 2;
		}

		return 8;
	}

	/**
	 * @param respuesta se debe enviar al ciente
	 */
	public void enviarRespuesta(String respuesta) {
		// Con el objeto de salida de datos envio la respuesta del servidor del estado
		// de sus aciertos
		salida.println(respuesta);
	}

	/**
	 * Cierra el servidor
	 */
	public void finSesion() {
		try {
			salida.close();
			entrada.close();
			socketCliente.close();
			socketServidor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("-> Servidor Terminado");
	}

	/**
	 * Metodo que genera una combinacion. NO MODIFICAR
	 */
	private void generarCombinacion() {
		Set<Integer> numeros = new TreeSet<Integer>();
		Random aleatorio = new Random();
		while (numeros.size() < 6) {
			numeros.add(aleatorio.nextInt(49) + 1);
		}
		int i = 0;
		this.combinacion = new int[6];
		for (Integer elto : numeros) {
			this.combinacion[i++] = elto;
		}
		this.reintegro = aleatorio.nextInt(49) + 1;
		this.complementario = aleatorio.nextInt(49) + 1;
	}

	/**
	 * Metodo que saca por consola del servidor la combinacion
	 */
	private void imprimirCombinacion() {
		System.out.print("Combinaci�n ganadora: ");
		for (Integer elto : this.combinacion)
			System.out.print(elto + " ");
		System.out.println("");
		System.out.println("Complementario:       " + this.complementario);
		System.out.println("Reintegro:            " + this.reintegro);
	}

}
