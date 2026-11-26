package BaseDatos;

import BaseDatos.VehiculoDAO;
import BaseDatos.Conexion;
import ManejoErrores.AccesoDatosException;
import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;
import Factory.VehiculoFactory;


public class VehiculoDAOImpl implements VehiculoDAO {

    private static final String COLUMNAS = "id, tipo, identificador, propietario, fecha_ingreso, fecha_salida, lavado";

    @Override
    public void insertar(Vehiculo v) throws AccesoDatosException {
        String sql = "INSERT INTO vehiculo (tipo, identificador, propietario, fecha_ingreso, fecha_salida, lavado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, v);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    v.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el vehiculo: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Vehiculo v) throws AccesoDatosException {
        String sql = "UPDATE vehiculo SET tipo = ?, identificador = ?, propietario = ?, fecha_ingreso = ?, "
                + "fecha_salida = ?, lavado = ? WHERE id = ?";
        try (Connection con = Conexion.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            llenar(ps, v);
            ps.setInt(7, v.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el vehiculo: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws AccesoDatosException {
        String sql = "DELETE FROM vehiculo WHERE id = ?";
        try (Connection con = Conexion.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el vehiculo: " + e.getMessage(), e);
        }
    }

    @Override
    public Vehiculo buscarPorId(int id) throws AccesoDatosException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculo WHERE id = ?";
        try (Connection con = Conexion.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? reconstruir(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el vehiculo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Vehiculo> listarTodos() throws AccesoDatosException {
        return consultar("SELECT " + COLUMNAS + " FROM vehiculo ORDER BY fecha_ingreso, id", null);
    }

    @Override
    public List<Vehiculo> listarDentro() throws AccesoDatosException {
        return consultar("SELECT " + COLUMNAS + " FROM vehiculo WHERE fecha_salida IS NULL ORDER BY fecha_ingreso", null);
    }

    @Override
    public List<Vehiculo> listarSalidosEnDia(LocalDate dia) throws AccesoDatosException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculo WHERE fecha_salida >= ? AND fecha_salida < ? ORDER BY fecha_salida";
        return consultar(sql, new Object[]{dia.atStartOfDay(), dia.plusDays(1).atStartOfDay()});
    }

    @Override
    public boolean existeDentro(String identificador, int excluirId) throws AccesoDatosException {
        String sql = "SELECT 1 FROM vehiculo WHERE identificador = ? AND fecha_salida IS NULL AND id <> ?";
        try (Connection con = Conexion.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identificador);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo verificar el vehiculo: " + e.getMessage(), e);
        }
    }


    private List<Vehiculo> consultar(String sql, Object[] parametros) throws AccesoDatosException {
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametros != null) {
                for (int i = 0; i < parametros.length; i++) {
                    ps.setObject(i + 1, parametros[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(reconstruir(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar la base de datos: " + e.getMessage(), e);
        }
        return lista;
    }

    private void llenar(PreparedStatement ps, Vehiculo v) throws SQLException {
        ps.setString(1, v.getTipo().name());
        ps.setString(2, v.getIdentificador());
        ps.setString(3, v.getPropietario());
        ps.setObject(4, v.getIngreso());
        if (v.getSalida() == null) {
            ps.setNull(5, Types.TIMESTAMP);
        } else {
            ps.setObject(5, v.getSalida());
        }
        ps.setBoolean(6, v.tieneLavado());
    }

//crea el objeto con su fabrica :b
    private Vehiculo reconstruir(ResultSet rs) throws SQLException, AccesoDatosException {
        try {
            TipoVehiculo tipo = TipoVehiculo.valueOf(rs.getString("tipo"));
            Vehiculo v = VehiculoFactory.crear(tipo, rs.getString("identificador"), rs.getString("propietario"),
                    rs.getObject("fecha_ingreso", LocalDateTime.class),
                    rs.getObject("fecha_salida", LocalDateTime.class),
                    rs.getBoolean("lavado"));
            v.setId(rs.getInt("id"));
            return v;
        } catch (IdentificadorInvalidoException | PropietarioInvalidoException | SalidaInvalidaException
                | IllegalArgumentException e) {
            throw new AccesoDatosException("Registro corrupto en la base de datos (id " + rs.getInt("id") + "): "
                    + e.getMessage(), e);
        }
    }
}
