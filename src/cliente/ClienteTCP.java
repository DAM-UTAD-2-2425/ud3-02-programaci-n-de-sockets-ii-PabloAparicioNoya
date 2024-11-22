package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * TODO: Complementa esta clase para que genere la conexi�n TCP con el servidor
 * para enviar un boleto, recibir la respuesta y finalizar la sesion
 */
public class ClienteTCP {
	// Creo objeto socket para permitir un puente de conexion
	private Socket socketCliente = null;
	// Creo dos objetos de lectura y salida de datos
	private BufferedReader entrada = null;
	private PrintWriter salida = null;

	/**
	 * Constructor
	 */
	public ClienteTCP(String ip, int puerto) {
		try {
			// Inicializo el atributo instanciandolo con su constructor, pasandole la ip y
			// el puerto
			socketCliente = new Socket(ip, puerto);
			System.out.println("Conexion establecida: " + socketCliente);
			// Creo dos objetos, uno para recibir datos y otro para enviarlos
			entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			salida = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketCliente.getOutputStream())), true);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.printf("Imposible conectar con ip:%s / puerto:%d", ip, puerto);
			System.exit(-1);
		}
	}

	/**
	 * @param combinacion que se desea enviar
	 * @return respuesta del servidor con la respuesta del boleto
	 */
	public String comprobarBoleto(int[] combinacion) {
		String numerosCliente = "";

		// Al recibirlo todo como un array de enteros, se debe pasar a String que es el
		// tipo de dato que solicita el servidor
		for (int i = 0; i < combinacion.length; i++) {
			if (i == combinacion.length - 1) {
				numerosCliente = numerosCliente + combinacion[i];
			} else {
				numerosCliente = numerosCliente + combinacion[i] + " ";
			}
		}

		// Se lo envio al servidor con el metodo de println
		salida.println(numerosCliente);

		// VAriable a la que le pasaré lo que se reciba del servidor
		String respuesta = "";

		try {
			respuesta = entrada.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return respuesta;
	}

	/**
	 * Sirve para finalizar la la conexi�n de Cliente y Servidor
	 */
	public void finSesion() {
		try {
			// Cierro todos los recursos utilizados
			salida.close();
			entrada.close();
			socketCliente.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Mensaje informativo al usuario de que el cliente ha finalizado
		System.out.println("-> Cliente Terminado");
	}

}
