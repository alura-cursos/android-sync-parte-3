package br.com.alura.agenda.sinc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import br.com.alura.agenda.ListaAlunosActivity;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.dto.AlunoSync;
import br.com.alura.agenda.event.AtualizaListaAlunoEvent;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.preferences.AlunoPreferences;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlunoSincronizador {
    private final Context context;
    private EventBus bus = EventBus.getDefault();
    private AlunoPreferences preferences;

    public AlunoSincronizador(Context context) {
        this.context = context;
        preferences = new AlunoPreferences(context);
    }

    public void buscaTodos(){
        if(preferences.temVersao()){
            buscaNovos();
        } else{
            buscaAlunos();
        }

    }

    private void buscaNovos() {
        String versao = preferences.getVersao();
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().novos(versao);
        call.enqueue(buscaAlunoCallback());
    }

    private void buscaAlunos() {
        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().lista();

        call.enqueue(buscaAlunoCallback());
    }

    @NonNull
    private Callback<AlunoSync> buscaAlunoCallback() {
        return new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync alunoSync = response.body();
                String versao = alunoSync.getMomentoDaUltimaModificacao();

                preferences.salvaVersao(versao);


                AlunoDAO dao = new AlunoDAO(context);
                dao.sincroniza(alunoSync.getAlunos());
                dao.close();

                Log.i("versao", preferences.getVersao());

                bus.post(new AtualizaListaAlunoEvent());
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {
                Log.e("onFailure chamado", t.getMessage());
                bus.post(new AtualizaListaAlunoEvent());
            }
        };
    }

    public void sincronizaAlunosInternos(){
        final AlunoDAO dao = new AlunoDAO(context);

        final List<Aluno> alunos = dao.listaNaoSincronizados();

        Call<AlunoSync> call = new RetrofitInicializador().getAlunoService().atualiza(alunos);

        call.enqueue(new Callback<AlunoSync>() {
            @Override
            public void onResponse(Call<AlunoSync> call, Response<AlunoSync> response) {
                AlunoSync alunoSync = response.body();
                dao.sincroniza(alunoSync.getAlunos());
                dao.close();
            }

            @Override
            public void onFailure(Call<AlunoSync> call, Throwable t) {

            }
        });


    }

    public void deleta(final Aluno aluno) {
        Call<Void> call = new RetrofitInicializador().getAlunoService().deleta(aluno.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                AlunoDAO dao = new AlunoDAO(context);
                dao.deleta(aluno);
                dao.close();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}