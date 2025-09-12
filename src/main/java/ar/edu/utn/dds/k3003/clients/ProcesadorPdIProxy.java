package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ProcesadorPdIProxy  {

    final private String endpoint;
    private final ProcesadorPdIRetrofitClient service;

    public ProcesadorPdIProxy(ObjectMapper objectMapper) {

        var env = System.getenv();
        this.endpoint = env.getOrDefault("ProcesadorPdI", "https://two025-dds-tp-procesadorpdi.onrender.com");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(ProcesadorPdIRetrofitClient.class);
    }

    public PdIDTO procesar(PdIDTO pdIDTO) throws java.io.IOException {
        var res = service.procesarPdi(pdIDTO).execute();
        if (!res.isSuccessful()) {
            throw new RuntimeException("Error conectandose con procesadorPdi (" +String.valueOf( res.code())+ ")");
        }
        return res.body();
    }

//    @Override
//    public PdIDTO buscarPdIPorId(String pdiId) throws NoSuchElementException {
//        return null;
//    }
//
//    @Override
//    public List<PdIDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
//        return List.of();
//    }
//
//    @Override
//    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
//
//    }
}