package ar.edu.utn.dds.k3003.app.controller;

import ar.edu.utn.dds.k3003.app.dtos.MensajeriaRequest;
import ar.edu.utn.dds.k3003.app.service.MessagingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mensajeria")
public class MensajeriaController {
    private final MessagingService messagingService;

    public MensajeriaController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping("/enviar")
    public String enviarMensaje(@RequestBody MensajeriaRequest request) {
        messagingService.enviarMensaje(request.getExchange(), request.getRoutingKey(), request.getMensaje());
        return "Mensaje enviado correctamente";
    }
}

