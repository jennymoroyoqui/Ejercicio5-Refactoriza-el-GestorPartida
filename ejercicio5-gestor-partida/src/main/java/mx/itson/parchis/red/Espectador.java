package mx.itson.parchis.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Escribe en la consola las jugadas que llegan por la red
 * Principio SRP el lado que escucha es otra responsabilidad distinta del lado que envia
 * link NotificadorSocket, así que vive en su propia clase
 */
public class Espectador {

    private final int puerto;

    public Espectador(int puerto) {
        this.puerto = puerto;
    }

    // Se queda escuchando hasta que se cierre el proceso
    public void escuchar() {
        System.out.println("Esperando jugadas en el puerto " + puerto + "...");
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            while (true) {
                try (Socket cliente = servidor.accept();
                        BufferedReader entrada = new BufferedReader(
                                new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = entrada.readLine()) != null) {
                        System.out.println("Llegó: " + linea);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo abrir el puerto " + puerto + ": " + e.getMessage());
        }
    }
}
