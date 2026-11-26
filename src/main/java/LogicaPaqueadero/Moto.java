package LogicaPaqueadero;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Moto extends Vehiculo implements Lavable {

    public static final long TARIFA_HORA = 1500;
    public static final long MAXIMO_BLOQUE_24H = 12000;
    public static final long TARIFA_LAVADO = 8000;
    private static final Pattern PATRON = Pattern.compile("[A-Z]{3}[0-9]{2}[A-Z]");

    private boolean lavado;

    public Moto(String identificador, String propietario, LocalDateTime ingreso, LocalDateTime salida, boolean lavado)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        super(identificador, propietario, ingreso, salida);
        this.lavado = lavado;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MOTO;
    }

    @Override
    protected Pattern getPatron() {
        return PATRON;
    }

    @Override
    protected String getEjemploIdentificador() {
        return "XYZ12D";
    }

    @Override
    public long calcularCobroParqueo() {
        if (estaDentro()) {
            return 0;
        }
        return cobroPorBloques(TARIFA_HORA, MAXIMO_BLOQUE_24H);
    }

    @Override
    public boolean isLavado() {
        return lavado;
    }

    @Override
    public void setLavado(boolean lavado) {
        this.lavado = lavado;
    }

    @Override
    public long getTarifaLavado() {
        return TARIFA_LAVADO;
    }

    @Override
    public boolean tieneLavado() {
        return lavado;
    }

    @Override
    public long calcularCostoLavado() {
        return lavado ? TARIFA_LAVADO : 0;
    }
}
