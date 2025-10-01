package ar.edu.utn.dds.k3003.app.dtos;

public class MensajeriaRequest {
    private String exchange;
    private String routingKey;
    private String mensaje;

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}

