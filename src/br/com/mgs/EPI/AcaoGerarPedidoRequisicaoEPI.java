package br.com.mgs.EPI;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;

public class AcaoGerarPedidoRequisicaoEPI implements AcaoRotinaJava {


    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        if( linhas.length == 0 ){
            contextoAcao.setMensagemRetorno("Favor seleciona pelo menos uma linha!");
        }else{
            for(Registro linha : linhas){
                ImportarPedidoRequisicaoEPI importarPedidoRequisicaoEPI = new ImportarPedidoRequisicaoEPI();
                importarPedidoRequisicaoEPI.setNumeroUnico((BigDecimal) linha.getCampo("NUIMPPED"));
                importarPedidoRequisicaoEPI.importar();
            }
        }
    }
}
