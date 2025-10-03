package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.mensajeria.K3003Worker;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.SimpleMessageConverter; // <-- IMPORTANTE
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ListenerService {

    private final ConnectionFactory connectionFactory;
    private final K3003Worker worker;
    private final RabbitAdmin rabbitAdmin;

    // Mapa para rastrear las suscripciones activas.
    private final java.util.Map<java.lang.String, SimpleMessageListenerContainer> activeTopicSubscriptions = new HashMap<>();

    @Autowired
    public ListenerService(ConnectionFactory connectionFactory, K3003Worker worker, RabbitAdmin rabbitAdmin) {
        this.connectionFactory = connectionFactory;
        this.worker = worker;
        this.rabbitAdmin = rabbitAdmin;
    }

    public java.lang.String crearTopicExchange(java.lang.String exchangeName) {
        try {
            TopicExchange newTopicExchange = new TopicExchange(exchangeName);
            rabbitAdmin.declareExchange(newTopicExchange);
            return "Topic Exchange '" + exchangeName + "' creado o ya existente.";
        } catch (java.lang.Exception e) {
            System.err.println("Error al crear Topic Exchange '" + exchangeName + "': " + e.getMessage());
            return "Error al crear Topic Exchange '" + exchangeName + "'.";
        }
    }

    public java.lang.String suscribirATopic(java.lang.String exchangeName, java.lang.String bindingPattern) {
        java.lang.String subscriptionKey = exchangeName + ":" + bindingPattern;
        if (activeTopicSubscriptions.containsKey(subscriptionKey)) {
            return "Ya existe una suscripción activa para el topic '" + bindingPattern + "' en el exchange '" + exchangeName + "'.";
        }

        Queue colaSuscriptor = new AnonymousQueue();
        rabbitAdmin.declareQueue(colaSuscriptor);

        Binding binding = BindingBuilder.bind(colaSuscriptor)
                .to(new TopicExchange(exchangeName))
                .with(bindingPattern);
        rabbitAdmin.declareBinding(binding);

        System.out.println("Suscribiendo a topic. Cola: '" + colaSuscriptor.getName() + "', Exchange: '" + exchangeName + "', Patrón: '" + bindingPattern + "'.");

        SimpleMessageListenerContainer container = createAndStartListener(colaSuscriptor.getName());
        activeTopicSubscriptions.put(subscriptionKey, container);

        return "Suscripción al topic '" + bindingPattern + "' iniciada. Escuchando en cola: " + colaSuscriptor.getName();
    }

    public java.lang.String quitarSuscripcionTopic(java.lang.String exchangeName, java.lang.String bindingPattern) {
        java.lang.String subscriptionKey = exchangeName + ":" + bindingPattern;
        SimpleMessageListenerContainer container = activeTopicSubscriptions.remove(subscriptionKey);
        if (container != null) {
            container.stop();
            return "Suscripción al topic '" + bindingPattern + "' detenida.";
        }
        return "No se encontró suscripción para el topic '" + bindingPattern + "'.";
    }

    // Método helper donde se aplica la corrección
    private SimpleMessageListenerContainer createAndStartListener(java.lang.String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);

        // --- INICIO DE LA CORRECCIÓN ---
        // 1. Creamos el adaptador que apunta a nuestro worker y su método
        MessageListenerAdapter adapter = new MessageListenerAdapter(worker, "procesarMensaje");

        // 2. Le decimos explícitamente que convierta el payload a String antes de llamar al método.
        // Esto soluciona el error "NoSuchMethodException: ...procesarMensaje([B)"
        adapter.setMessageConverter(new SimpleMessageConverter());
        // --- FIN DE LA CORRECCIÓN ---

        container.setMessageListener(adapter);
        container.start();
        return container;
    }

    public java.util.Set<java.lang.String> obtenerListenersActivos() {
        java.util.Set<java.lang.String> todosLosActivos = new HashSet<>();
        activeTopicSubscriptions.forEach((key, value) -> {
            String[] parts = key.split(":", 2);
            if (parts.length == 2) {
                todosLosActivos.add("Suscripción a Topic: exchange='" + parts[0] + "', patron='" + parts[1] + "'");
            }
        });
        return todosLosActivos;
    }
}
