package mx.itson.parchis.modelo;

import java.util.EnumMap;
import java.util.Map;

/**
 * La forma del tablero qué casilla del recorrido, del pasillo o de la base
 * corresponde a cada posición de una ficha, no dibuja nada ni conoce las reglas
 * del juego esta geometría estaba mezclada con el dibujo y con las reglas
 * dentro de code GestorPartida la fórmula code (salida + posición) % 68
 * aparecía en la captura y en el dibujo aquí vive una sola vez
 */
public class Tablero {

    public static final int CELDAS = 19;
    public static final int TOTAL_CASILLAS = 68;
    public static final int ULTIMA_DEL_RECORRIDO = 66;
    public static final int META = 74;
    public static final int CASILLAS_PASILLO = 7;

    private record DisenoColor(int columnaBase, int[] origenPasillo, int[] pasoPasillo, int[][] lugaresMeta) {

    }

    private final Map<ColorJugador, DisenoColor> disenos = new EnumMap<>(ColorJugador.class);

    public Tablero() {
        disenos.put(ColorJugador.ROJO, new DisenoColor(0,
                new int[]{9, 1}, new int[]{0, 1},
                new int[][]{{9, 8}, {10, 8}, {10, 9}, {9, 9}}));
        disenos.put(ColorJugador.AZUL, new DisenoColor(11,
                new int[]{1, 9}, new int[]{1, 0},
                new int[][]{{8, 9}, {8, 10}, {9, 10}, {8, 8}}));
    }

    public int casillaDelRecorrido(ColorJugador color, int posicion) {
        return (color.getCasillaSalida() + posicion) % TOTAL_CASILLAS;
    }

    public int[] coordenadaCasilla(int i) {
        if (i <= 7) {
            return new int[]{8, i};
        }
        if (i <= 15) {
            return new int[]{15 - i, 8};
        }
        if (i == 16) {
            return new int[]{0, 9};
        }
        if (i <= 24) {
            return new int[]{i - 17, 10};
        }
        if (i <= 32) {
            return new int[]{8, i - 14};
        }
        if (i == 33) {
            return new int[]{9, 18};
        }
        if (i <= 41) {
            return new int[]{10, 52 - i};
        }
        if (i <= 49) {
            return new int[]{i - 31, 10};
        }
        if (i == 50) {
            return new int[]{18, 9};
        }
        if (i <= 58) {
            return new int[]{69 - i, 8};
        }
        if (i <= 66) {
            return new int[]{10, 66 - i};
        }
        return new int[]{9, 0};
    }

    public int[] coordenadaPasillo(ColorJugador color, int i) {
        DisenoColor diseno = disenos.get(color);
        return new int[]{
            diseno.origenPasillo()[0] + i * diseno.pasoPasillo()[0],
            diseno.origenPasillo()[1] + i * diseno.pasoPasillo()[1]};
    }

    public int columnaBase(ColorJugador color) {
        return disenos.get(color).columnaBase();
    }

    public double[] centroDeFicha(ColorJugador color, int numero, int posicion) {
        double fila;
        double columna;
        if (posicion == Jugador.EN_BASE) {
            double[][] lugares = {{2.6, 2.6}, {2.6, 5.4}, {5.4, 2.6}, {5.4, 5.4}};
            fila = lugares[numero][0];
            columna = columnaBase(color) + lugares[numero][1];
        } else if (posicion == META) {

            int[][] lugares = disenos.get(color).lugaresMeta();
            fila = lugares[numero][0] + 0.5;
            columna = lugares[numero][1] + 0.5;
        } else {
            int[] celda = posicion <= ULTIMA_DEL_RECORRIDO
                    ? coordenadaCasilla(casillaDelRecorrido(color, posicion))
                    : coordenadaPasillo(color, posicion - 67);
            // Un pequeño desplazamiento para que dos fichas en la misma casilla se distingan
            double desplazamiento = (numero - 1.5) * 0.12;
            fila = celda[0] + 0.5 + desplazamiento;
            columna = celda[1] + 0.5 + desplazamiento;
        }
        return new double[]{fila, columna};
    }
}
