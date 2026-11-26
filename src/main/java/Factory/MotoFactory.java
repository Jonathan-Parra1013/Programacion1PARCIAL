package Factory;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.LocalDateTime;
import LogicaPaqueadero.Moto;
import LogicaPaqueadero.Vehiculo;

public class MotoFactory extends VehiculoFactory {

    @Override
    public Vehiculo crearVehiculo(String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        return new Moto(identificador, propietario, ingreso, salida, lavado);
    }
}
