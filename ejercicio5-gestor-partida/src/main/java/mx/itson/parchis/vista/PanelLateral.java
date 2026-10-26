package mx.itson.parchis.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * El panel de la derecha turno, dado, botones, bitácora y estadísticas solo
 * muestra texto y avisa de los botones, no sabe qué significa cada botón recibe
 * qué hacer como link Runnable, de modo que no depende de la partida
 */
public class PanelLateral extends JPanel {

    private final JLabel etiquetaTurno = new JLabel(" ");
    private final JLabel etiquetaDado = new JLabel("Dado: -");
    private final JLabel etiquetaEstadisticas = new JLabel(" ");
    private final JButton botonDado = new JButton("Lanzar dado");
    private final JButton botonNueva = new JButton("Nueva partida");
    private final JTextArea bitacora = new JTextArea();

    public PanelLateral(Runnable alLanzarDado, Runnable alIniciarNueva) {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(300, 620));

        JPanel arriba = new JPanel(new GridLayout(0, 1, 0, 8));
        JLabel titulo = new JLabel("Parchís");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        etiquetaTurno.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        etiquetaDado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        botonDado.addActionListener(e -> alLanzarDado.run());
        botonNueva.addActionListener(e -> alIniciarNueva.run());
        arriba.add(titulo);
        arriba.add(etiquetaTurno);
        arriba.add(etiquetaDado);
        arriba.add(botonDado);
        arriba.add(botonNueva);
        add(arriba, BorderLayout.NORTH);

        bitacora.setEditable(false);
        bitacora.setLineWrap(true);
        bitacora.setWrapStyleWord(true);
        bitacora.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        add(new JScrollPane(bitacora), BorderLayout.CENTER);

        etiquetaEstadisticas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        add(etiquetaEstadisticas, BorderLayout.SOUTH);
    }

    public void mostrarTurno(String texto, Color color) {
        etiquetaTurno.setText(texto);
        etiquetaTurno.setForeground(color);
    }

    public void mostrarDado(String texto) {
        etiquetaDado.setText(texto);
    }

    public void habilitarDado(boolean habilitado) {
        botonDado.setEnabled(habilitado);
    }

    public void mostrarEstadisticas(String html) {
        etiquetaEstadisticas.setText(html);
    }

    public void agregarBitacora(String texto) {
        bitacora.append(texto + "\n");
        bitacora.setCaretPosition(bitacora.getDocument().getLength());
    }

    public void limpiarBitacora() {
        bitacora.setText("");
    }
}
