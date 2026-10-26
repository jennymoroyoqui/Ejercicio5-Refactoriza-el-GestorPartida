package mx.itson.parchis.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import mx.itson.parchis.red.NotificadorJugadas;

public class Partida {

    private static final int MAX_SEIS_SEGUIDOS = 3;
    private static final int SALIR_DE_LA_BASE = 5;

    private final Tablero tablero;
    private final NotificadorJugadas notificador;
    private final Consumer<String> bitacora;
    private final List<Jugador> jugadores = new ArrayList<>();
    private final List<String> jugadas = new ArrayList<>();
    private final Random azar = new Random();

    private int indiceTurno = 0;
    private int dado = 0;
    private boolean yaTiro = false;
    private int seisSeguidos = 0;
    private boolean terminada = false;
    private Jugador ganador = null;

    public Partida(Tablero tablero, NotificadorJugadas notificador, Consumer<String> bitacora) {
        this.tablero = tablero;
        this.notificador = notificador;
        this.bitacora = bitacora;
        jugadores.add(new Jugador("Jugador 1", ColorJugador.ROJO));
        jugadores.add(new Jugador("Jugador 2", ColorJugador.AZUL));
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public Jugador getJugadorEnTurno() {
        return jugadores.get(indiceTurno);
    }

    public int getDado() {
        return dado;
    }

    public boolean haTirado() {
        return yaTiro;
    }

    public boolean isTerminada() {
        return terminada;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public List<String> getJugadas() {
        return jugadas;
    }

    public String motivoNoPuedeLanzar() {
        if (terminada) {
            return "La partida termino. Empieza una nueva.";
        }
        if (yaTiro) {
            return "Ya tiraste: elige una ficha.";
        }
        return null;
    }

    //Lanza el dado al azar y lo aplica al jugador en turno
    public void lanzarDado() {
        procesarDado(azar.nextInt(6) + 1);
    }

    //Aplica el valor del dado al jugador en turno
    public void procesarDado(int valor) {
        Jugador jugador = getJugadorEnTurno();
        dado = valor;
        yaTiro = true;
        if (dado == 6) {
            seisSeguidos++;
        } else {
            seisSeguidos = 0;
        }
        bitacora.accept(jugador.getNombre() + " saco " + dado + ".");
        notificador.enviar("DADO;" + jugador.getColor().getNombre() + ";" + dado);

        if (!tieneAlgunaJugada(jugador)) {
            bitacora.accept("No puede mover ninguna ficha.");
            yaTiro = false;
            pasarTurnoOVolverATirar();
        }
    }

    private boolean tieneAlgunaJugada(Jugador jugador) {
        for (int i = 0; i < Jugador.FICHAS; i++) {
            if (fichaTieneJugada(jugador.getPosicion(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean fichaTieneJugada(int posicion) {
        if (posicion == Jugador.EN_BASE) {
            return dado == SALIR_DE_LA_BASE;
        }
        return posicion < Tablero.META && posicion + dado <= Tablero.META;
    }

    public boolean esMovible(Jugador jugador, int ficha) {
        return jugador == getJugadorEnTurno() && yaTiro && !terminada
                && fichaTieneJugada(jugador.getPosicion(ficha));
    }

    public String motivoNoPuedeMover(int ficha) {
        int posicion = getJugadorEnTurno().getPosicion(ficha);
        if (posicion == Tablero.META) {
            return "Esa ficha ya llegó a la meta.";
        }
        if (posicion == Jugador.EN_BASE && dado != SALIR_DE_LA_BASE) {
            return "Para sacar una ficha de la base necesitas un 5.";
        }
        if (posicion >= 0 && posicion + dado > Tablero.META) {
            return "Esa ficha necesita exactamente un " + (Tablero.META - posicion)
                    + " para llegar a la meta.";
        }
        return null;
    }

    public void moverFicha(int numero) {
        Jugador jugador = getJugadorEnTurno();
        if (jugador.getPosicion(numero) == Jugador.EN_BASE) {
            jugador.setPosicion(numero, 0);
            bitacora.accept(jugador.getNombre() + " sacó la ficha " + (numero + 1) + " de su base.");
        } else {
            jugador.setPosicion(numero, jugador.getPosicion(numero) + dado);
            bitacora.accept(jugador.getNombre() + " avanzó " + dado + (dado == 1 ? " casilla" : " casillas")
                    + " con la ficha " + (numero + 1) + ".");
        }
        int posicion = jugador.getPosicion(numero);
        jugadas.add(jugador.getColor().getNombre() + "," + numero + "," + posicion);

        capturarRivales(jugador, posicion);
        notificador.enviar("MOVIMIENTO;" + jugador.getColor().getNombre() + ";" + numero + ";" + posicion);

        if (posicion == Tablero.META) {
            bitacora.accept("La ficha " + (numero + 1) + " de " + jugador.getNombre() + " llegó a la meta.");
        }
        yaTiro = false;
        if (jugador.fichasEnMeta() == Jugador.FICHAS) {
            terminarPartida(jugador);
            return;
        }
        pasarTurnoOVolverATirar();
    }

    private void capturarRivales(Jugador jugador, int posicion) {
        if (posicion > Tablero.ULTIMA_DEL_RECORRIDO) {
            return;
        }
        int casilla = tablero.casillaDelRecorrido(jugador.getColor(), posicion);
        Jugador rival = rivalDe(jugador);
        for (int j = 0; j < Jugador.FICHAS; j++) {
            int posicionRival = rival.getPosicion(j);
            if (posicionRival >= 0 && posicionRival <= Tablero.ULTIMA_DEL_RECORRIDO
                    && tablero.casillaDelRecorrido(rival.getColor(), posicionRival) == casilla) {
                rival.setPosicion(j, Jugador.EN_BASE);
                bitacora.accept("¡Capturó la ficha " + (j + 1) + " de " + rival.getNombre()
                        + "! Regresa a su base.");
                notificador.enviar("CAPTURA;" + rival.getColor().getNombre() + ";" + j);
            }
        }
    }

    private Jugador rivalDe(Jugador jugador) {
        return jugador == jugadores.get(0) ? jugadores.get(1) : jugadores.get(0);
    }

    private void pasarTurnoOVolverATirar() {
        if (dado == 6 && seisSeguidos < MAX_SEIS_SEGUIDOS) {
            bitacora.accept("Sacó 6: vuelve a tirar.");
        } else {
            cambiarTurno();
        }
    }

    private void cambiarTurno() {
        indiceTurno = (indiceTurno + 1) % jugadores.size();
        seisSeguidos = 0;
        Jugador jugador = getJugadorEnTurno();
        bitacora.accept("Turno de " + jugador.getNombre() + " (" + jugador.getColor().getNombre() + ").");
    }

    private void terminarPartida(Jugador jugadorGanador) {
        terminada = true;
        ganador = jugadorGanador;
        bitacora.accept("¡" + jugadorGanador.getNombre() + " ganó la partida!");
        notificador.enviar("FIN;" + jugadorGanador.getColor().getNombre());
    }

    //Regresa todo al inicio: fichas en la base y turno del rojo
    public void reiniciar() {
        for (Jugador jugador : jugadores) {
            jugador.reiniciar();
        }
        indiceTurno = 0;
        dado = 0;
        yaTiro = false;
        seisSeguidos = 0;
        terminada = false;
        ganador = null;
        jugadas.clear();
        bitacora.accept("Nueva partida. Empieza " + getJugadorEnTurno().getNombre() + ".");
    }
}
