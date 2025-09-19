package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProcesadorPdIRetrofitClient {
    @POST("/api/pdis")
    Call<PdIDTO> procesarPdi(@Body PdIDTO pdi);
}