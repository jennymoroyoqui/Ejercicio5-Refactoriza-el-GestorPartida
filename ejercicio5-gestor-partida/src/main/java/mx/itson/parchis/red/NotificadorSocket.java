package mx.itson.parchis.red;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Envia cada mensaje a la computadora del rival por un socket TCP es la unica
 * clase que conoce code Socket, si el rival no contesta, avisa una sola vez por
 * el canal code alFallar y la partida sigue en esta computadora
 */
public class NotificadorSocket implements NotificadorJugadas {

    private static final int TIEMPO_ESPERA_MS = 300;

    private final String host;
    private final int puerto;
    private final Consumer<String> alFallar;
    private boolean avisoSinRed = false;

    public NotificadorSocket(String host, int puerto, Consumer<String> alFallar) {
        this.host = host;
        this.puerto = puerto;
        this.alFallar = alFallar;
    }

    @Override
    public void enviar(String mensaje) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), TIEMPO_ESPERA_MS);
            PrintWriter salida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            salida.println(mensaje);
        } catch (IOException e) {
            if (!avisoSinRed) {
                avisoSinRed = true;
                alFallar.accept("Sin conexión con " + host + ":" + puerto
                        + ". Se sigue jugando en esta computadora.");
            }
        }
    }
}
