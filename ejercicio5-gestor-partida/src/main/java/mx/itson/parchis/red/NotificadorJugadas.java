package mx.itson.parchis.red;

/**
 * Lo único que la partida necesita saber de la red: poder avisar que algo pasó
 * code Partida depende de este contrato y no de code Socket, se puede cambiar
 * el socket por otro medio (o por un notificador de prueba que no haga nada)
 * sin tocar las reglas del juego
 */
public interface NotificadorJugadas {

    void enviar(String mensaje);
}
