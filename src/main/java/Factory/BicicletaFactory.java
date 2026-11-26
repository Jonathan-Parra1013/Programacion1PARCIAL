package Factory;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.LocalDateTime;
import LogicaPaqueadero.Bicicleta;
import LogicaPaqueadero.Vehiculo;

public class BicicletaFactory extends VehiculoFactory {

    @Override
    public Vehiculo crearVehiculo(String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        return new Bicicleta(identificador, propietario, ingreso, salida);
    }
}
