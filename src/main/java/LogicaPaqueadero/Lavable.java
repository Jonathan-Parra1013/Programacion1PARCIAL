package LogicaPaqueadero;

//Solo lo implementan Carro y Moto
public interface Lavable {

    boolean isLavado();

    void setLavado(boolean lavado);

    //Valor del lavado cuando se pide
    long getTarifaLavado();
}
