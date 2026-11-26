package BaseDatos;

import ManejoErrores.AccesoDatosException;
import java.time.LocalDate;
import java.util.List;
import LogicaPaqueadero.Vehiculo;

public interface VehiculoDAO {


    void insertar(Vehiculo v) throws AccesoDatosException;

    void actualizar(Vehiculo v) throws AccesoDatosException;

    void eliminar(int id) throws AccesoDatosException;

    Vehiculo buscarPorId(int id) throws AccesoDatosException;

    List<Vehiculo> listarTodos() throws AccesoDatosException;


    List<Vehiculo> listarDentro() throws AccesoDatosException;


    List<Vehiculo> listarSalidosEnDia(LocalDate dia) throws AccesoDatosException;


    boolean existeDentro(String identificador, int excluirId) throws AccesoDatosException;
}
