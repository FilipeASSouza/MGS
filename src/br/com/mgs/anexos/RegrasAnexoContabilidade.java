package br.com.mgs.anexos;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.util.Collection;
import java.util.Iterator;

public class RegrasAnexoContabilidade {

    private static String numeroUnico;
    private static JapeWrapper importacaoPlanilhaITEDAO = JapeFactory.dao("AD_TCBIMPMANITE"); //AD_TCBIMPMANITE
    private static JapeWrapper mestreLotesDAO = JapeFactory.dao("MestreLote"); //TCBLOT
    private static JapeWrapper lancamentosContabeisDAO = JapeFactory.dao("Lancamento"); //TCBLAN
    private static JapeWrapper usuariosDAO = JapeFactory.dao("Usuario"); // TSIUSU

    public static void validarAnexoImportacaoPlanilhaLotesContabeis(PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        numeroUnico = vo.asString("PKREGISTRO").replace(String.valueOf("_AD_TCBIMPMANCAB"), String.valueOf(""));

        if( vo.asString("NOMEINSTANCIA").equalsIgnoreCase(String.valueOf("AD_TCBIMPMANCAB")) ){

            Collection<DynamicVO> importacaoPlanilhaITENSVO = importacaoPlanilhaITEDAO.find("NUIMPMAN = ?", new Object[]{ numeroUnico });
            Iterator lancamentos = importacaoPlanilhaITENSVO.iterator();

            while( lancamentos.hasNext() ){

                DynamicVO importacaoPlanilhaITE = (DynamicVO) lancamentos.next();
                DynamicVO lancamentosContabeisVO = lancamentosContabeisDAO.findOne("NUMDOC = ? AND SEQUENCIA = ? AND NUMLOTE = ? AND CODCTACTB = ? "
                        , new Object[]{importacaoPlanilhaITE.asBigDecimal("DOCUMENTO"), importacaoPlanilhaITE.asBigDecimal("SEQUENCIA")
                        , importacaoPlanilhaITE.asBigDecimal("NUMLOTE"), importacaoPlanilhaITE.asBigDecimal("CODCTACTB")});

                // Verificando a situação do lote e a data de competencia "Fechado"

                if(lancamentosContabeisVO != null){

                    DynamicVO mestreLoteVO = mestreLotesDAO.findOne("CODEMP = ? AND REFERENCIA = ? AND NUMLOTE = ? "
                            , new Object[]{ importacaoPlanilhaITE.asBigDecimal("CODEMP"), lancamentosContabeisVO.asTimestamp("REFERENCIA")
                            , importacaoPlanilhaITE.asBigDecimal("NUMLOTE")});

                    DynamicVO usuarioVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

                    if( mestreLoteVO != null && mestreLoteVO.asString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                            & ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                        ErroUtils.disparaErro("Periodo contabil fechado, fineza verificar!");
                    }
                }
            }
        }
    }
}
