package br.com.alura.agenda.services;


import java.util.List;

import br.com.alura.agenda.dto.AlunoSync;
import br.com.alura.agenda.modelo.Aluno;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by alura on 11/29/16.
 */

public interface AlunoService {

    @POST("aluno")
    Call<Void> insere(@Body Aluno aluno);

    @GET("aluno")
    Call<AlunoSync> lista();

    @DELETE("aluno/{id}")
    Call<Void> deleta(@Path("id") String id);

    @GET("aluno/diff")
    Call<AlunoSync> novos(@Header("datahora") String versao);

    @PUT("aluno/lista")
    Call<AlunoSync> atualiza(@Body List<Aluno> alunos);
}
