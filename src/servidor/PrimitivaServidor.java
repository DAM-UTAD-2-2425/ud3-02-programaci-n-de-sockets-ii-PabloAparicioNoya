package servidor;

public class PrimitivaServidor {

	public static void main(String[] args) {
		ServidorTCP canal = new ServidorTCP(5555);
		String linea;
		String respuesta;
		do {
			linea = canal.leerCombinacion();
			// Le he añadido un recurso de salida del bucle dado que en caso de cierre de
			// cliente el servidor deberá manejar un dato null y esto lo llevará a una
			// excepcion
			if (linea == null) {
				// DE Esta manera acabo el bucle sin conflictos
				linea = "FIN";
			} else {
				respuesta = canal.comprobarBoleto(linea);
				canal.enviarRespuesta(respuesta);
			}
		} while (!linea.equals("FIN"));
		canal.finSesion();

	}

}
