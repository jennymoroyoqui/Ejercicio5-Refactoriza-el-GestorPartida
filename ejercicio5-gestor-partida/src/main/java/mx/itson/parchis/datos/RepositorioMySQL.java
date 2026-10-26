package mx.itson.parchis.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mx.itson.parchis.modelo.ColorJugador;

//Guarda las estadisticas en MySQL, principio SRP unica clase que conoce SQL y code java.sql 
public class RepositorioMySQL implements RepositorioEstadisticas {

    private final String url;
    private final String usuario;
    private final String clave;
    private boolean disponible = false;

    //Intenta crear la tabla; si no hay servidor, el repositorio queda no disponible y el juego sigue
    public RepositorioMySQL(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
        crearTabla();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private void crearTabla() {
        try (Connection conexion = conectar();
                Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate("CREATE TABLE IF NOT EXISTS estadisticas ("
                    + "color VARCHAR(10) PRIMARY KEY, "
                    + "jugadas INT NOT NULL DEFAULT 0, "
                    + "ganadas INT NOT NULL DEFAULT 0)");
            disponible = true;
        } catch (SQLException e) {
            disponible = false;
            System.out.println("Sin base de datos: " + e.getMessage());
        }
    }

    @Override
    public boolean estaDisponible() {
        return disponible;
    }

    @Override
    public void registrarPartida(ColorJugador ganador) throws ErrorDeAlmacenamiento {
        String sql = "INSERT INTO estadisticas (color, jugadas, ganadas) VALUES (?, 1, ?) "
                + "ON DUPLICATE KEY UPDATE jugadas = jugadas + 1, ganadas = ganadas + ?";
        try (Connection conexion = conectar();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (ColorJugador color : ColorJugador.values()) {
                int gano = color == ganador ? 1 : 0;
                sentencia.setString(1, color.getNombre());
                sentencia.setInt(2, gano);
                sentencia.setInt(3, gano);
                sentencia.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ErrorDeAlmacenamiento(e.getMessage());
        }
    }

    @Override
    public List<Estadistica> consultar() throws ErrorDeAlmacenamiento {
        List<Estadistica> estadisticas = new ArrayList<>();
        try (Connection conexion = conectar();
                Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery(
                        "SELECT color, jugadas, ganadas FROM estadisticas ORDER BY color DESC")) {
            while (resultado.next()) {
                estadisticas.add(new Estadistica(resultado.getString("color"),
                        resultado.getInt("jugadas"), resultado.getInt("ganadas")));
            }
        } catch (SQLException e) {
            throw new ErrorDeAlmacenamiento(e.getMessage());
        }
        return estadisticas;
    }
}
