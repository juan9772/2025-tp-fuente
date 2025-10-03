package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.ListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/topics")
public class ListenerController {

    private final ListenerService listenerService;

    @Autowired
    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    /**
     * Endpoint para CREAR un nuevo Topic Exchange.
     * Ejemplo: POST /api/topics/otro-exchange-de-hechos
     */
    @PostMapping("/{exchangeName}")
    public ResponseEntity<java.lang.String> crearTopicExchange(@PathVariable java.lang.String exchangeName) {
        java.lang.String resultado = listenerService.crearTopicExchange(exchangeName);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para SUSCRIBIRSE a un topic dentro de un exchange.
     * Se debe enviar un JSON en el body con el patrón de binding.
     * Ejemplo: POST /api/topics/hechos-topic-exchange/suscripciones
     * Body: { "patron": "hechos.deportes.*" }
     */
    @PostMapping("/{exchangeName}/suscripciones")
    public ResponseEntity<java.lang.String> agregarSuscripcionTopic(@PathVariable java.lang.String exchangeName, @RequestBody java.util.Map<java.lang.String, java.lang.String> body) {
        java.lang.String bindingPattern = body.get("patron");
        if (bindingPattern == null || bindingPattern.isBlank()) {
            return ResponseEntity.badRequest().body("El body debe contener la clave 'patron' con el topic a suscribir.");
        }
        java.lang.String resultado = listenerService.suscribirATopic(exchangeName, bindingPattern);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para CANCELAR una suscripción.
     * Ejemplo: DELETE /api/topics/hechos-topic-exchange/suscripciones
     * Body: { "patron": "hechos.deportes.*" }
     */
    @DeleteMapping("/{exchangeName}/suscripciones")
    public ResponseEntity<java.lang.String> quitarSuscripcionTopic(@PathVariable java.lang.String exchangeName, @RequestBody java.util.Map<java.lang.String, java.lang.String> body) {
        java.lang.String bindingPattern = body.get("patron");
        if (bindingPattern == null || bindingPattern.isBlank()) {
            return ResponseEntity.badRequest().body("El body debe contener la clave 'patron' del topic a cancelar.");
        }
        java.lang.String resultado = listenerService.quitarSuscripcionTopic(exchangeName, bindingPattern);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para ver todas las suscripciones activas.
     * Ejemplo: GET /api/topics/suscripciones
     */
    @GetMapping("/suscripciones")
    public ResponseEntity<java.util.Set<java.lang.String>> obtenerSuscripcionesActivas() {
        return ResponseEntity.ok(listenerService.obtenerListenersActivos());
    }
}
