package LogicaPaqueadero;

import ManejoErrores.IdentificadorInvalidoException;
import ManejoErrores.PropietarioInvalidoException;
import ManejoErrores.SalidaInvalidaException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;


public abstract class Vehiculo {

    private int id; // llave de la BD (0 = aun no guardado)
    private String identificador;
    private String propietario;
    private LocalDateTime ingreso;
    private LocalDateTime salida; // null mientras esta dentro

    protected Vehiculo(String identificador, String propietario, LocalDateTime ingreso, LocalDateTime salida)
            throws IdentificadorInvalidoException, PropietarioInvalidoException, SalidaInvalidaException {
        setIdentificador(identificador);
        setPropietario(propietario);
        setIngreso(ingreso);
        setSalida(salida);
    }

    public abstract TipoVehiculo getTipo();


    protected abstract Pattern getPatron();


    protected abstract String getEjemploIdentificador();


    public abstract long calcularCobroParqueo();


    public boolean tieneLavado() {
        return false;
    }

    public long calcularCostoLavado() {
        return 0;
    }

    public final long calcularTotal() {
        if (estaDentro()) {
            return 0;
        }
        return calcularCobroParqueo() + calcularCostoLavado();
    }



    protected long horasCobradas() {
        long segundos = Duration.between(ingreso, salida).getSeconds();
        return (segundos + 3599) / 3600;
    }

    /** Cobro de carro/moto: bloques de 24 h al maximo + horas restantes sin superar el maximo. */
    protected long cobroPorBloques(long tarifaHora, long maximoBloque) {
        long horas = horasCobradas();
        long bloques = horas / 24;
        long resto = horas % 24;
        return bloques * maximoBloque + Math.min(resto * tarifaHora, maximoBloque);
    }

    public boolean estaDentro() {
        return salida == null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentificador() {
        return identificador;
    }

    public final void setIdentificador(String identificador) throws IdentificadorInvalidoException {
        String limpio = identificador == null ? "" : identificador.trim().toUpperCase();
        if (!getPatron().matcher(limpio).matches()) {
            throw new IdentificadorInvalidoException("Identificador '" + (identificador == null ? "" : identificador.trim())
                    + "' invalido para " + getTipo().getEtiqueta() + ". Formato esperado: " + getEjemploIdentificador());
        }
        this.identificador = limpio;
    }

    public String getPropietario() {
        return propietario;
    }

    public final void setPropietario(String propietario) throws PropietarioInvalidoException {
        if (propietario == null || propietario.trim().isEmpty()) {
            throw new PropietarioInvalidoException("El nombre del propietario no puede estar vacio.");
        }
        this.propietario = propietario.trim();
    }

    public LocalDateTime getIngreso() {
        return ingreso;
    }

    public final void setIngreso(LocalDateTime ingreso) throws SalidaInvalidaException {
        if (ingreso == null) {
            throw new IllegalArgumentException("La fecha y hora de ingreso es obligatoria.");
        }
        if (salida != null && !salida.isAfter(ingreso)) {
            throw new SalidaInvalidaException("La salida (" + salida + ") debe ser posterior al ingreso (" + ingreso + ").");
        }
        this.ingreso = ingreso;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public final void setSalida(LocalDateTime salida) throws SalidaInvalidaException {
        if (salida != null && !salida.isAfter(ingreso)) {
            throw new SalidaInvalidaException("La salida (" + salida + ") debe ser posterior al ingreso (" + ingreso + ").");
        }
        this.salida = salida;
    }

    @Override
    public String toString() {
        return getTipo().getEtiqueta() + " - " + identificador + " - " + propietario;
    }
}
