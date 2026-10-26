package mx.itson.parchis.modelo;

public class Jugador {

    public static final int FICHAS = 4;
    public static final int EN_BASE = -1;

    private final String nombre;
    private final ColorJugador color;
    private final int[] fichas = new int[FICHAS];

    public Jugador(String nombre, ColorJugador color) {
        this.nombre = nombre;
        this.color = color;
        reiniciar();
    }

    public String getNombre() {
        return nombre;
    }

    public ColorJugador getColor() {
        return color;
    }

    public int getPosicion(int ficha) {
        return fichas[ficha];
    }

    public void setPosicion(int ficha, int posicion) {
        fichas[ficha] = posicion;
    }

    //Regresa las cuatro fichas a la base
    public void reiniciar() {
        for (int i = 0; i < FICHAS; i++) {
            fichas[i] = EN_BASE;
        }
    }

    //Cuantas fichas han llegado a la meta
    public int fichasEnMeta() {
        int enMeta = 0;
        for (int posicion : fichas) {
            if (posicion == Tablero.META) {
                enMeta++;
            }
        }
        return enMeta;
    }
}
