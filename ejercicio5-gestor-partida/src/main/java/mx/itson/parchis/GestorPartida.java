package mx.itson.parchis;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import mx.itson.parchis.datos.RepositorioEstadisticas;
import mx.itson.parchis.datos.RepositorioMySQL;
import mx.itson.parchis.modelo.Jugador;
import mx.itson.parchis.modelo.Partida;
import mx.itson.parchis.modelo.Tablero;
import mx.itson.parchis.red.Espectador;
import mx.itson.parchis.red.NotificadorJugadas;
import mx.itson.parchis.red.NotificadorSocket;
import mx.itson.parchis.vista.PanelLateral;
import mx.itson.parchis.vista.VistaTablero;

/**
 * crea las piezas, las conecta y reacciona a lo que hace el usuario, ahora solo
 * ensambla link Partida, link VistaTablero, link PanelLateral, un link
 * NotificadorJugadas y un link RepositorioEstadisticas
 */
public class GestorPartida {

    // Red: se avisa de cada jugada
    private static final String HOST_RIVAL = "localhost";
    private static final int PUERTO_RIVAL = 5000;

    // Base de datos
    private static final String URL_BD = "jdbc:mysql://localhost:3306/parchis?createDatabaseIfNotExist=true&connectTimeout=2000";
    private static final String USUARIO_BD = "root";
    private static final String CLAVE_BD = "";

    private final PanelLateral panel;
    private final VistaTablero vista;
    private final Partida partida;
    private final RepositorioEstadisticas estadisticas;

    public GestorPartida() {
        Tablero tablero = new Tablero();
        panel = new PanelLateral(this::lanzarDado, this::nuevaPartida);
        NotificadorJugadas notificador = new NotificadorSocket(HOST_RIVAL, PUERTO_RIVAL, panel::agregarBitacora);
        partida = new Partida(tablero, notificador, panel::agregarBitacora);
        vista = new VistaTablero(partida, tablero, this::elegirFicha);
        estadisticas = new RepositorioMySQL(URL_BD, USUARIO_BD, CLAVE_BD);
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("espectador")) {
            new Espectador(PUERTO_RIVAL).escuchar();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            GestorPartida gestor = new GestorPartida();
            gestor.mostrarVentana();
        });
    }

    private void mostrarVentana() {
        JFrame ventana = new JFrame("Parchís");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.add(vista, BorderLayout.CENTER);
        ventana.add(panel, BorderLayout.EAST);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        mostrarEstadisticas();
        Jugador jugador = partida.getJugadorEnTurno();
        panel.agregarBitacora("Empieza " + jugador.getNombre() + " (" + jugador.getColor().getNombre() + ").");
        refrescar();
        ventana.setVisible(true);
    }

    private void lanzarDado() {
        String motivo = partida.motivoNoPuedeLanzar();
        if (motivo != null) {
            JOptionPane.showMessageDialog(vista, motivo);
            return;
        }
        partida.lanzarDado();
        refrescar();
    }

    private void elegirFicha(int ficha) {
        String motivo = partida.motivoNoPuedeMover(ficha);
        if (motivo != null) {
            JOptionPane.showMessageDialog(vista, motivo);
            return;
        }
        partida.moverFicha(ficha);
        if (partida.isTerminada()) {
            guardarEstadisticas(partida.getGanador());
            mostrarEstadisticas();
            refrescar();
            Jugador ganador = partida.getGanador();
            JOptionPane.showMessageDialog(vista,
                    "¡Ganó " + ganador.getNombre() + " (" + ganador.getColor().getNombre() + ")!");
            return;
        }
        refrescar();
    }

    private void nuevaPartida() {
        panel.limpiarBitacora();
        partida.reiniciar();
        refrescar();
    }

    //Pone en la pantalla lo que dice la partida turno, dado, boton y tablero
 
    private void refrescar() {
        Jugador jugador = partida.getJugadorEnTurno();
        if (partida.isTerminada()) {
            panel.mostrarTurno("Partida terminada", Color.DARK_GRAY);
        } else {
            panel.mostrarTurno("Turno: " + jugador.getNombre() + " (" + jugador.getColor().getNombre() + ")",
                    VistaTablero.colorAwt(jugador.getColor()));
        }
        int dado = partida.getDado();
        panel.mostrarDado("Dado: " + (dado == 0 ? "-" : String.valueOf(dado))
                + (partida.haTirado() ? " · elige una ficha" : ""));
        panel.habilitarDado(!partida.isTerminada() && !partida.haTirado());
        vista.repaint();
    }

    private void guardarEstadisticas(Jugador ganador) {
        if (!estadisticas.estaDisponible()) {
            panel.agregarBitacora("Sin base de datos: no se guardaron las estadisticas");
            return;
        }
        try {
            estadisticas.registrarPartida(ganador.getColor());
        } catch (RepositorioEstadisticas.ErrorDeAlmacenamiento e) {
            panel.agregarBitacora("No se guardaron las estadisticas: " + e.getMessage());
        }
    }

    private void mostrarEstadisticas() {
        if (!estadisticas.estaDisponible()) {
            panel.mostrarEstadisticas("<html>Estadisticas: sin base de datos</html>");
            return;
        }
        String texto = "<html><b>Estadisticas</b>";
        try {
            for (RepositorioEstadisticas.Estadistica fila : estadisticas.consultar()) {
                texto += "<br>" + fila.color() + ": " + fila.ganadas() + " ganadas de " + fila.jugadas();
            }
        } catch (RepositorioEstadisticas.ErrorDeAlmacenamiento e) {
            texto += "<br>No se pudieron leer: " + e.getMessage();
        }
        panel.mostrarEstadisticas(texto + "</html>");
    }
}
