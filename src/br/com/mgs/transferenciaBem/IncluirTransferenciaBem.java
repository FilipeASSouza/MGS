package br.com.mgs.transferenciaBem;

import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class IncluirTransferenciaBem implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        if (contextoAcao.getLinhas().length < 1) {
            ErroUtils.disparaErro("Favor selecionar um ou mais registros !");
        }

        try {
            if (contextoAcao.confirmarSimNao("Confirma Transferência de Bens", "Os Bens serão transferidos para o local informado, deseja confirmar ?", 1)) {
                JapeWrapper transcabDAO = JapeFactory.dao("AD_TCICABTRANS");
                BigDecimal codusu = AuthenticationInfo.getCurrent().getUserID();
                String dttransf = TimeUtils.formataDDMMYYYY(TimeUtils.getToday());
                FluidCreateVO ad_tcicabtrans = JapeFactory.dao("AD_TCICABTRANS").create();
                ad_tcicabtrans.set("CODUSU", codusu);
                ad_tcicabtrans.set("DTTRANSF", contextoAcao.getParam("DTTRANSFIN"));
                ad_tcicabtrans.set("MOTIVOTRANSF", contextoAcao.getParam("MOTIVOTRANSF"));
                BigDecimal codtrans = ad_tcicabtrans.save().asBigDecimal("CODTRANS");
                if (codtrans != null) {
                    Registro[] linhas = contextoAcao.getLinhas();
                    Registro[] var8 = linhas;
                    int var9 = linhas.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        Registro linha = var8[var10];
                        String bembaixado = this.bemBaixado(linha.getCampo("CODPROD"), linha.getCampo("CODBEM"));
                        if ("S" == bembaixado) {
                            ErroUtils.disparaErro("Bem " + linha.getCampo("CODBEM") + " não pode ser transferido. <br>Bem já baixado!!!");
                        }

                        JapeWrapper transiteDAO = JapeFactory.dao("AD_TCIITETRANS");
                        FluidCreateVO iteVO = transiteDAO.create();
                        iteVO.set("CODTRANS", codtrans);
                        iteVO.set("CODEMP", linha.getCampo("CODEMP"));
                        iteVO.set("CODBEM", linha.getCampo("CODBEM"));
                        iteVO.set("CODPROD", linha.getCampo("CODPROD"));
                        iteVO.set("CODLOC", new BigDecimal((String)contextoAcao.getParam("CODLOCIN")));
                        iteVO.set("CODLOT", new BigDecimal((String)contextoAcao.getParam("CODLOTIN")));
                        iteVO.set("CODCENCUS", new BigDecimal((String)contextoAcao.getParam("CODCENCUSIN")));
                        iteVO.set("MATRICULA", new BigDecimal(Long.parseLong((String) contextoAcao.getParam("MATRICULAIN"))));
                        iteVO.save();
                    }

                    contextoAcao.setMensagemRetorno("Transferência " + codtrans + " realizada com sucesso !!! ");
                } else {
                    ErroUtils.disparaErro("Não foi possível gravar os dados da transferência! Favor tentar novamente.");
                }
            }

        } catch (Exception var15) {
            throw new IllegalStateException(var15);
        }
    }

    private String bemBaixado(Object codprod, Object codbem) throws Exception {
        JapeWrapper tcibemDAO = JapeFactory.dao("Imobilizado");
        String baixado = "N";
        DynamicVO baixadoVO = tcibemDAO.findOne("CODPROD=? AND CODBEM=? AND DTBAIXA IS NOT NULL", new Object[]{codprod, codbem});
        if (baixadoVO != null) {
            baixado = "S";
        }

        return baixado;
    }
}
