package br.com.mgs.EPI;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;

import java.math.BigDecimal;

public class AcaoConfirmarPedidosRequisicaoEPI implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        JapeWrapper usuariosDAO = JapeFactory.dao("Usuario");
        DynamicVO usuariosVO;

        usuariosVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

        BigDecimal codigoUsuario = usuariosVO.asBigDecimalOrZero("CODUSU");
        String nomeUsuario = usuariosVO.asString("NOMEUSU");

        if( linhas.length == 0 ){
            contextoAcao.setMensagemRetorno("Favor selecionar pelo menos um lançamento!");
        }else{
            for(Registro linha : linhas ){
                AuthenticationInfo authenticationInfo = new AuthenticationInfo(nomeUsuario, codigoUsuario, BigDecimal.ZERO, 0);
                authenticationInfo.makeCurrent();
                AuthenticationInfo.getCurrent();

                BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
                barramentoConfirmacao.setValidarSilencioso(true);
                ConfirmacaoNotaHelper.confirmarNota((BigDecimal) linha.getCampo("NUNOTA"), barramentoConfirmacao);
            }
        }


    }
}
