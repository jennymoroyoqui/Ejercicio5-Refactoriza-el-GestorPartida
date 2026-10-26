package mx.itson.parchis.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import mx.itson.parchis.modelo.ColorJugador;
import mx.itson.parchis.modelo.Jugador;
import mx.itson.parchis.modelo.Partida;
import mx.itson.parchis.modelo.Tablero;

/**
 * Dibuja el tablero y las fichas, avisa cuando el usuario hace clic en una
 * ficha solo conoce píxeles y colores de pantalla, las reglas están en link
 * Partida y la forma del tablero en link Tablero un solo método convierte link
 * ColorJugador en link Color y un solo bucle dibuja a cualquier jugador, en
 * lugar de repetir el código para rojo y para azul
 */
public class VistaTablero extends JPanel {

    private static final Color COLOR_BASE_SIN_USAR = new Color(0xD9DDD3);
    private static final Color COLOR_PASILLO_SIN_USAR = new Color(0xE7EAE3);

    private final Partida partida;
    private final Tablero tablero;

    // Medidas del tablero en pantalla; se calculan al dibujar
    private int tamCelda;
    private int origenX;
    private int origenY;

    public VistaTablero(Partida partida, Tablero tablero, IntConsumer alElegirFicha) {
        this.partida = partida;
        this.tablero = tablero;
        setPreferredSize(new Dimension(620, 620));
        setBackground(new Color(0xF3F4EF));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int ficha = fichaEn(e.getX(), e.getY());
                if (ficha >= 0) {
                    alElegirFicha.accept(ficha);
                }
            }
        });
    }

    /**
     * La ficha del jugador en turno bajo ese punto, o -1 si no hay ninguna (o
     * no es momento de elegir)
     */
    private int fichaEn(int x, int y) {
        if (partida.isTerminada() || !partida.haTirado()) {
            return -1;
        }
        Jugador jugador = partida.getJugadorEnTurno();
        for (int i = 0; i < Jugador.FICHAS; i++) {
            int[] centro = centroFicha(jugador.getColor(), i, jugador.getPosicion(i));
            int dx = x - centro[0];
            int dy = y - centro[1];
            int radio = tamCelda / 2;
            if (dx * dx + dy * dy <= radio * radio) {
                return i;
            }
        }
        return -1;
    }

    //El unico lugar donde un color del juego se vuelve un color de pantalla
    public static Color colorAwt(ColorJugador color) {
        return new Color(color.getRgb());
    }

    private static Color aclarar(Color color) {
        return new Color(
                (color.getRed() + 2 * 255) / 3,
                (color.getGreen() + 2 * 255) / 3,
                (color.getBlue() + 2 * 255) / 3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        tamCelda = (Math.min(getWidth(), getHeight()) - 20) / Tablero.CELDAS;
        origenX = (getWidth() - tamCelda * Tablero.CELDAS) / 2;
        origenY = (getHeight() - tamCelda * Tablero.CELDAS) / 2;

        // Bases: rojo arriba a la izquierda y azul arriba a la derecha las de abajo no se usan
        for (ColorJugador color : ColorJugador.values()) {
            dibujarBase(g2, 0, tablero.columnaBase(color), colorAwt(color));
        }
        dibujarBase(g2, 11, 0, COLOR_BASE_SIN_USAR);
        dibujarBase(g2, 11, 11, COLOR_BASE_SIN_USAR);

        // Recorrido, con la salida de cada jugador en su color
        for (int i = 0; i < Tablero.TOTAL_CASILLAS; i++) {
            int[] celda = tablero.coordenadaCasilla(i);
            Color relleno = Color.WHITE;
            for (Jugador jugador : partida.getJugadores()) {
                if (i == jugador.getColor().getCasillaSalida()) {
                    relleno = aclarar(colorAwt(jugador.getColor()));
                    break;
                }
            }
            pintarCelda(g2, celda[0], celda[1], relleno);
        }

        // Pasillos hacia la meta
        for (int i = 0; i < Tablero.CASILLAS_PASILLO; i++) {
            for (Jugador jugador : partida.getJugadores()) {
                int[] celda = tablero.coordenadaPasillo(jugador.getColor(), i);
                pintarCelda(g2, celda[0], celda[1], aclarar(colorAwt(jugador.getColor())));
            }
            // Los pasillos de los colores que no juegan
            pintarCelda(g2, 9, 17 - i, COLOR_PASILLO_SIN_USAR);
            pintarCelda(g2, 17 - i, 9, COLOR_PASILLO_SIN_USAR);
        }

        g2.setColor(new Color(0x14233A));
        g2.fillRect(origenX + 8 * tamCelda, origenY + 8 * tamCelda, 3 * tamCelda, 3 * tamCelda);
        g2.setColor(new Color(0xA9B6C6));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(8, tamCelda * 3 / 10)));
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString("META", origenX + 10 * tamCelda + tamCelda / 2 - medidas.stringWidth("META") / 2,
                origenY + 10 * tamCelda + tamCelda / 2 + medidas.getAscent() / 2 - 1);

        for (Jugador jugador : partida.getJugadores()) {
            dibujarFichas(g2, jugador);
        }
    }

    private void dibujarBase(Graphics2D g2, int fila, int columna, Color color) {
        g2.setColor(color);
        g2.fillRect(origenX + columna * tamCelda, origenY + fila * tamCelda, 8 * tamCelda, 8 * tamCelda);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(origenX + (columna + 1) * tamCelda, origenY + (fila + 1) * tamCelda,
                6 * tamCelda, 6 * tamCelda, tamCelda, tamCelda);
    }

    private void pintarCelda(Graphics2D g2, int fila, int columna, Color relleno) {
        int x = origenX + columna * tamCelda;
        int y = origenY + fila * tamCelda;
        g2.setColor(relleno);
        g2.fillRect(x, y, tamCelda, tamCelda);
        g2.setColor(new Color(0xB9C0B5));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, tamCelda, tamCelda);
    }

    private void dibujarFichas(Graphics2D g2, Jugador jugador) {
        int radio = tamCelda * 2 / 5;
        for (int i = 0; i < Jugador.FICHAS; i++) {
            int[] centro = centroFicha(jugador.getColor(), i, jugador.getPosicion(i));

            // Resaltar las fichas que se pueden mover
            if (partida.esMovible(jugador, i)) {
                g2.setColor(new Color(0x2A7A50));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(centro[0] - radio - 4, centro[1] - radio - 4, (radio + 4) * 2, (radio + 4) * 2);
            }

            g2.setColor(colorAwt(jugador.getColor()));
            g2.fillOval(centro[0] - radio, centro[1] - radio, radio * 2, radio * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(centro[0] - radio, centro[1] - radio, radio * 2, radio * 2);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, radio)));
            String numero = String.valueOf(i + 1);
            FontMetrics medidas = g2.getFontMetrics();
            g2.drawString(numero, centro[0] - medidas.stringWidth(numero) / 2,
                    centro[1] + medidas.getAscent() / 2 - 1);
        }
    }

    private int[] centroFicha(ColorJugador color, int numero, int posicion) {
        double[] centro = tablero.centroDeFicha(color, numero, posicion);
        return new int[]{
            (int) Math.round(origenX + centro[1] * tamCelda),
            (int) Math.round(origenY + centro[0] * tamCelda)};
    }
}
