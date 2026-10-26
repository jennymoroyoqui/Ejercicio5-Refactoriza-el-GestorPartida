package mx.itson.parchis.modelo;

/**
 * Los colores con los que se puede jugar Principio DRY antes el color era un
 * code String rojo/azul comparado con code equals en doce lugares y la casilla
 * de salida y el tono vivian repetidos en code Jugador y en code GestorPartida
 * ahora cada dato del color se define una sola vez Principio de composicion
 * sobre herencia la diferencia entre rojo y azul es informacion como nombre,
 * salida, tono, no comportamiento, se modela como dato y no como subclases
 */
public enum ColorJugador {

    ROJO("rojo", 1, 0xC23B2E),
    AZUL("azul", 18, 0x2563A8);

    private final String nombre;
    private final int casillaSalida;
    private final int rgb;

    ColorJugador(String nombre, int casillaSalida, int rgb) {
        this.nombre = nombre;
        this.casillaSalida = casillaSalida;
        this.rgb = rgb;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCasillaSalida() {
        return casillaSalida;
    }

    public int getRgb() {
        return rgb;
    }
}
