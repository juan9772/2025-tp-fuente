package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.config.RabbitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para emitir eventos hacia otros módulos mediante RabbitMQ.
 * Implementa el lado "productor" de la arquitectura event-driven.
 */
@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Emite un evento cuando se crea un nuevo hecho.
     * El agregador escuchará este evento y creará un índice de búsqueda.
     */
    public void emitirHechoCreado(String hechoId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "HECHO_CREADO");
            event.put("hechoId", hechoId);
            event.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.TOPIC_EXCHANGE_NAME,
                    "hecho.creado",
                    json
            );

            log.info("✅ Evento HECHO_CREADO emitido para hecho: {}", hechoId);
        } catch (JsonProcessingException e) {
            log.error("❌ Error al emitir evento HECHO_CREADO para hecho: {}", hechoId, e);
        }
    }

    /**
     * Emite un evento cuando se actualiza el estado de un hecho.
     * El agregador re-indexará el hecho con los cambios.
     */
    public void emitirHechoActualizado(String hechoId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "HECHO_ACTUALIZADO");
            event.put("hechoId", hechoId);
            event.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.TOPIC_EXCHANGE_NAME,
                    "hecho.actualizado",
                    json
            );

            log.info("✅ Evento HECHO_ACTUALIZADO emitido para hecho: {}", hechoId);
        } catch (JsonProcessingException e) {
            log.error("❌ Error al emitir evento HECHO_ACTUALIZADO para hecho: {}", hechoId, e);
        }
    }
}
