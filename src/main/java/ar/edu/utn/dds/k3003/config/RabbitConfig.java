package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.TopicExchange; // <-- IMPORTANTE: Cambiamos a TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Nombre de nuestro topic exchange principal.
    public static final java.lang.String TOPIC_EXCHANGE_NAME = "hechos-topic-exchange";

    // Define el bean para nuestro exchange de tipo topic.
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    // RabbitTemplate para enviar mensajes
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    // El RabbitAdmin sigue siendo crucial para crear y gestionar todo dinámicamente.
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
