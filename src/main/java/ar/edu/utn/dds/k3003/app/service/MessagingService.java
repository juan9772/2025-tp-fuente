package ar.edu.utn.dds.k3003.app.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagingService {
    private final RabbitTemplate rabbitTemplate;

    public MessagingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarMensaje(String exchange, String routingKey, String mensaje) {
        rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
    }
}

