package br.com.mgs.utils;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PesquisarMatricula {

    public PesquisarMatricula() {
    }

    public PesquisarMatricula.DadosFuncionario getFuncionario(String linha) {
        Gson gson = new Gson();
        return gson.fromJson(linha, DadosFuncionario.class);
    }

    public class RowMatricula {
        public String MATRICULA;
        public String NOME;
        public String EMAIL;
        public String AREA_RISCO;
        public String SITUACAO;
        public BigDecimal LOTACAO;
        public BigDecimal SAPATO;
        public BigDecimal CALCA;
        public BigDecimal CAMISA;
        public BigDecimal PALETO;
        public String DATA_INI_VIGENCIA;

        public RowMatricula() {
        }
    }

    public class ListMatricula {
        public List<PesquisarMatricula.RowMatricula> RowMatricula;

        public ListMatricula() {
        }
    }

    public class DadosFuncionario {
        public PesquisarMatricula.ListMatricula ListMatricula;

        public DadosFuncionario() {
        }
    }
}
