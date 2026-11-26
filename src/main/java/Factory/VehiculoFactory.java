package Factory;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import LogicaPaqueadero.TipoVehiculo;
import LogicaPaqueadero.Vehiculo;


public abstract class VehiculoFactory {

    private static final Map<TipoVehiculo, VehiculoFactory> FABRICAS = new EnumMap<>(TipoVehiculo.class);

    static {
        FABRICAS.put(TipoVehiculo.CARRO, new CarroFactory());
        FABRICAS.put(TipoVehiculo.MOTO, new MotoFactory());
        FABRICAS.put(TipoVehiculo.BICICLETA, new BicicletaFactory());
    }

    public abstract Vehiculo crearVehiculo(String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException;

    public static VehiculoFactory obtener(TipoVehiculo tipo) {
        return FABRICAS.get(tipo);
    }


    public static Vehiculo crear(TipoVehiculo tipo, String identificador, String propietario,
            LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        return obtener(tipo).crearVehiculo(identificador, propietario, ingreso, salida, lavado);
    }
}
