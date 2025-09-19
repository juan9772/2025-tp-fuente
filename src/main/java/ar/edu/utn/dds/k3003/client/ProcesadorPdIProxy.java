package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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