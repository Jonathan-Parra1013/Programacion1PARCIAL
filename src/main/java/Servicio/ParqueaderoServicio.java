package Servicio;

import ManejoErrores.AccesoDatosException;
import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import ManejoErrores.VehiculoYaDentroException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;
import Factory.VehiculoFactory;
import BaseDatos.VehiculoDAO;
import BaseDatos.VehiculoDAOImpl;


public class ParqueaderoServicio {

    private final VehiculoDAO dao;

    public ParqueaderoServicio() {
        this(new VehiculoDAOImpl());
    }

    public ParqueaderoServicio(VehiculoDAO dao) {
        this.dao = dao;
    }


    public Vehiculo registrar(TipoVehiculo tipo, String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException,
            VehiculoYaDentroException, AccesoDatosException {
        Vehiculo v = VehiculoFactory.crear(tipo, identificador, propietario, ingreso, salida, lavado);
        verificarNoEstaDentro(v.getIdentificador(), 0);
        dao.insertar(v);
        return v;
    }

   
    public Vehiculo actualizar(int id, TipoVehiculo tipo, String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException,
            VehiculoYaDentroException, AccesoDatosException {
        Vehiculo v = VehiculoFactory.crear(tipo, identificador, propietario, ingreso, salida, lavado);
        v.setId(id);
        verificarNoEstaDentro(v.getIdentificador(), id);
        dao.actualizar(v);
        return v;
    }

 
    public Vehiculo registrarSalida(int id, LocalDateTime salida) throws SalidaInvalidaException, AccesoDatosException {
        Vehiculo v = dao.buscarPorId(id);
        if (v == null) {
            throw new AccesoDatosException("El vehiculo ya no existe en la base de datos.", null);
        }
        v.setSalida(salida);
        dao.actualizar(v);
        return v;
    }


    public void eliminar(int id) throws AccesoDatosException {
        dao.eliminar(id);
    }


    public List<Vehiculo> listarTodos() throws AccesoDatosException {
        return dao.listarTodos();
    }


    public List<Vehiculo> vehiculosDentro() throws AccesoDatosException {
        return dao.listarDentro();
    }


    public Map<TipoVehiculo, Long> recaudoPorTipo(LocalDate dia) throws AccesoDatosException {
        Map<TipoVehiculo, Long> totales = new EnumMap<>(TipoVehiculo.class);
        for (TipoVehiculo t : TipoVehiculo.values()) {
            totales.put(t, 0L);
        }
        for (Vehiculo v : dao.listarSalidosEnDia(dia)) {
            totales.merge(v.getTipo(), v.calcularTotal(), Long::sum);
        }
        return totales;
    }

    private void verificarNoEstaDentro(String identificador, int excluirId)
            throws VehiculoYaDentroException, AccesoDatosException {
        if (dao.existeDentro(identificador, excluirId)) {
            throw new VehiculoYaDentroException("El vehiculo " + identificador
                    + " ya esta dentro del parqueadero y no ha registrado salida.");
        }
    }
}
