package LogicaPaqueadero;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

//La bicicleta NO implementa Lavable
public class Bicicleta extends Vehiculo {

    public static final long MINUTOS_GRATIS = 30;
    public static final long TARIFA_HORA = 500;
    private static final Pattern PATRON = Pattern.compile("BIC-[0-9]{4}");

    public Bicicleta(String identificador, String propietario, LocalDateTime ingreso, LocalDateTime salida)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        super(identificador, propietario, ingreso, salida);
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.BICICLETA;
    }

    @Override
    protected Pattern getPatron() {
        return PATRON;
    }

    @Override
    protected String getEjemploIdentificador() {
        return "BIC-0042";
    }

    //Primeros 30 min gratis; despues $500 por hora (fraccion = hora completa) contada desde el ingreso :D
    @Override
    public long calcularCobroParqueo() {
        if (estaDentro()) {
            return 0;
        }
        long segundos = Duration.between(getIngreso(), getSalida()).getSeconds();
        if (segundos <= MINUTOS_GRATIS * 60) {
            return 0;
        }
        return horasCobradas() * TARIFA_HORA;
    }
}
