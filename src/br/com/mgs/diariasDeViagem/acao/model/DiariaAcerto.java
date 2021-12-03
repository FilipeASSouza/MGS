package br.com.mgs.diariasDeViagem.acao.model;

import com.google.gson.Gson;

import java.util.List;

public class DiariaAcerto {

    public DiariaAcerto(){ }

    public DiariaAcerto.DadosDiariaAcerto getDiariaAcerto(String linha){
        Gson gson = new Gson();
        DiariaAcerto.DadosDiariaAcerto dadosDiariaAcerto = (DiariaAcerto.DadosDiariaAcerto) gson.fromJson( linha, DiariaAcerto.DadosDiariaAcerto.class );
        return dadosDiariaAcerto;
    }

    public class rowDiariaAcerto {
        public String cod_autorizacao_viagem;
        public String cnpjParceiro;
        public String matricula;
        public String dataNegociacao;
        public String dataVencimento;
        public String codigoBanco;
        public String codigoTipoTitulo;
        public String unidade;
        public String codigoBancoParceiro;
        public String codigoAgenciaParceiro;
        public String contaParceiro;
        public String valorDesdobramento;
        public String possuiAdiantamento;
        public String tipoConta;
        public String contrato;
        public String modalidade;
        public String tipoServiço;

        public rowDiariaAcerto(){ }
    }

    public class ListDiariaAcerto{
        public List<DiariaAcerto.rowDiariaAcerto> rowDiariaAcertos;
        public ListDiariaAcerto() {}
    }

    public class DadosDiariaAcerto{
        public DiariaAcerto.ListDiariaAcerto ListDiariaAcerto;
        public DadosDiariaAcerto(){ }
    }
}
