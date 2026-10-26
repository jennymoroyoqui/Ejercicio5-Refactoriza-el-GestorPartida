package mx.itson.parchis.datos;

import java.util.List;
import mx.itson.parchis.modelo.ColorJugador;

/**
 * Contrato para guardar y consultar las estadisticas de las partidas
 * Diseño de la interfaz quien usa las estadisticas no sabe si detras hay MySQL, un
 * archivo o una lista en memoria y ningun tipo de code java.sql se escapa por aqui
 */
public interface RepositorioEstadisticas {

    //Jugadas y victorias acumuladas de un color
    record Estadistica(String color, int jugadas, int ganadas) { }

    //Falla al leer o escribir oculta la causa tecnica SQL, archivo
    class ErrorDeAlmacenamiento extends Exception {
        public ErrorDeAlmacenamiento(String mensaje) {
            super(mensaje);
        }
    }

    //code false si no se pudo preparar el almacen
    boolean estaDisponible();

    //Suma una partida jugada a cada color y una victoria al ganador
    void registrarPartida(ColorJugador ganador) throws ErrorDeAlmacenamiento;

    List<Estadistica> consultar() throws ErrorDeAlmacenamiento;
}
