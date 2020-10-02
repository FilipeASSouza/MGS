package br.com.mgs.duplicapedido;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class DuplicaPedidoCorte extends DuplicaPedidoUtils implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        JapeWrapper wmsDAO = JapeFactory.dao("Separacao");
        JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");
        Registro[] var4 = linhas;
        int var5 = linhas.length;

        for (int var6 = 0; var6 < var5; var6++) {
            Registro linha = var4[var6];
            if (!linha.getCampo("TIPMOV").equals("J")) {
                contextoAcao.setMensagemRetorno("Somete Pedidos de Requisição podem ser Duplicados, TIPMOV = 'J' ");
                return;
            }

            DynamicVO wmsVO = wmsDAO.findOne("NUNOTA = ? ", linha.getCampo("NUNOTA"));
            if (wmsVO == null) {
                contextoAcao.setMensagemRetorno("Somete Pedidos que ja foram enviados para o WMS podem ser duplicados");
                return;
            }

            if (linha.getCampo("AD_NUNOTAPED") != null) {

                DynamicVO notaOrigemVO = cabDAO.findOne("NUNOTA = ?", linha.getCampo("AD_NUNOTAPED"));

                if( notaOrigemVO == null ){

                    duplicaPeloCorte((BigDecimal) linha.getCampo("NUNOTA"));

                } else if (notaOrigemVO.asBigDecimal("NUNOTA") != null) {

                    contextoAcao.setMensagemRetorno("Pedido já foi duplicado Anteriormente, Nro. Unico: " + linha.getCampo("AD_NUNOTAPED").toString());
                    return;

                }
            }else if (linha.getCampo("AD_NUNOTAPED") == null){
                duplicaPeloCorte((BigDecimal) linha.getCampo("NUNOTA"));
            }
        }
    }
}
