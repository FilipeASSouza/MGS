package br.com.mgs.contabilidadeImportacaoManual;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class GerarDadosContabeisGerenciaisAcao {

    public GerarDadosContabeisGerenciaisAcao() {
    }

    public void doAction(ContextoAcao contextoAcao) throws Exception {

        ( new ImportacaoManualController()).gerarDadosContabeis(contextoAcao);
        ( new ImportacaoManualController()).gerarDadosGerenciais(contextoAcao);
    }
}
