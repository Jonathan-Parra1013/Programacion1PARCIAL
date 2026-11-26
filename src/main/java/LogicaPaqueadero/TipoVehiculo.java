package LogicaPaqueadero;

//Tipos de vehiculo que recibe el parqueadero. toString() 
public enum TipoVehiculo {
    CARRO("Carro", true),
    MOTO("Moto", true),
    BICICLETA("Bicicleta", false);

    private final String etiqueta;
    private final boolean admiteLavado;

    TipoVehiculo(String etiqueta, boolean admiteLavado) {
        this.etiqueta = etiqueta;
        this.admiteLavado = admiteLavado;
    }

    /** Sirve para habilitar/deshabilitar el checkbox de lavado en la ventana (R6). */
    public boolean admiteLavado() {
        return admiteLavado;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
