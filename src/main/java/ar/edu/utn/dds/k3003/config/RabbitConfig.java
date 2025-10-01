package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue mspsbdsjQueue() {
        // durable=true para que la cola persista
        return new Queue("mspsbdsj", true);
    }
}
