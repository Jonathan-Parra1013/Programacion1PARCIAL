package BaseDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexiones JDBC hacia MySQL (XAMPP por defecto).
 * Ajuste USUARIO y CLAVE si su MySQL los tiene distintos.
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/parklibre?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private Conexion() {
    }

    /** Devuelve una conexion nueva. Quien la pida debe cerrarla (try-con-recursos). */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
