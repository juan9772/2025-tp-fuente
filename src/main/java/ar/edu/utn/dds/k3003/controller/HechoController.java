package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.dtos.EstadoBorradoEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class HechoController {

    private final Fachada fachadaFuente;

    @Autowired
    public HechoController(Fachada fachadaFuente) {
        this.fachadaFuente = fachadaFuente;
    }

    //    @GetMapping
    //    public ResponseEntity<List<HechoDTO>> listarHechos() {
    //        return ResponseEntity.ok(fachadaFuente.Hechos());
    //    }

    @GetMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> obtenerHecho(@PathVariable String id) {
        return ResponseEntity.ok(fachadaFuente.buscarHechoXId(id));
    }

    @PostMapping("/hecho")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO Hecho) {
        return ResponseEntity.ok(fachadaFuente.agregar(Hecho));
    }

    @PatchMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> actualizarEstadoHecho(@PathVariable String id, @RequestBody Map<String, String> estadoData) {
        try {
            String estado = estadoData.get("estado");
            return ResponseEntity.ok(fachadaFuente.modificar(id, EstadoBorradoEnum.valueOf(estado)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
