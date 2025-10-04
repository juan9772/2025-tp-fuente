package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record PdI_DTO(
        String id,
        @JsonProperty("hecho_id")
        String hechoId,
        String descripcion,
        String lugar,
        LocalDateTime momento,
        String contenido,

        @JsonProperty("imagen_url")
        String imagenUrl,

        @JsonProperty("ocr_texto")
        String ocrTexto,

        @JsonProperty("etiquetas_auto")
        List<String> etiquetasAuto
) {

    public PdI_DTO(String id,String hechoId) {
        this(id,hechoId, null, null, null, null, null, null, List.of());
    }

    public PdI_DTO(String id,String hechoId, String descripcion, String lugar, LocalDateTime momento,
                   String contenido, String imagenUrl){
        this(id,hechoId, descripcion, lugar, momento, contenido, imagenUrl, null, List.of());
    }

}
