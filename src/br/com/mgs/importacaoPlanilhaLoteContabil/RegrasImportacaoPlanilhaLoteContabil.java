package br.com.mgs.importacaoPlanilhaLoteContabil;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.util.Collection;
import java.util.Iterator;

public class RegrasImportacaoPlanilhaLoteContabil {

    private static JapeWrapper importacaoPlanilhaITEDAO = JapeFactory.dao("AD_TCBIMPMANITE"); //AD_TCBIMPMANITE
    private static JapeWrapper lancamentosContabeisDAO = JapeFactory.dao("Lancamento"); //TCBLAN
    private static JapeWrapper usuariosDAO = JapeFactory.dao("Usuario"); // TSIUSU

    public static void validaExclusaoImportacaoPlanilhaLoteContabil(PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        Collection<DynamicVO> importacaoPlanilhaITENSVO = importacaoPlanilhaITEDAO.find("NUIMPMAN = ?", new Object[]{ vo.asBigDecimal("NUIMPMAN") });
        Iterator lancamentos = importacaoPlanilhaITENSVO.iterator();

        while( lancamentos.hasNext() ){

            DynamicVO importacaoPlanilhaITE = (DynamicVO) lancamentos.next();
            DynamicVO lancamentosContabeisVO = lancamentosContabeisDAO.findOne("NUMDOC = ? AND SEQUENCIA = ? AND NUMLOTE = ? AND CODCTACTB = ? "
                    , new Object[]{importacaoPlanilhaITE.asBigDecimal("DOCUMENTO"), importacaoPlanilhaITE.asBigDecimal("SEQUENCIA")
                            , importacaoPlanilhaITE.asBigDecimal("NUMLOTE"), importacaoPlanilhaITE.asBigDecimal("CODCTACTB")});

            // Verificando se o usuario tem permissao para excluir os registros na importacao da planilha

            DynamicVO usuarioVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

            if( lancamentosContabeisVO != null
                        & ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                    ErroUtils.disparaErro("Lançamentos contabeis gerados, não é permitido excluir! Fineza verificar!");
            }
        }
    }

    public static void validaAtualizacaoImportacaoPlanilhaLoteContabil (PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        Collection<DynamicVO> importacaoPlanilhaITENSVO = importacaoPlanilhaITEDAO.find("NUIMPMAN = ?", new Object[]{ vo.asBigDecimal("NUIMPMAN") });
        Iterator lancamentos = importacaoPlanilhaITENSVO.iterator();

        while( lancamentos.hasNext() ){

            DynamicVO importacaoPlanilhaITE = (DynamicVO) lancamentos.next();
            DynamicVO lancamentosContabeisVO = lancamentosContabeisDAO.findOne("NUMDOC = ? AND SEQUENCIA = ? AND NUMLOTE = ? AND CODCTACTB = ? "
                    , new Object[]{importacaoPlanilhaITE.asBigDecimal("DOCUMENTO"), importacaoPlanilhaITE.asBigDecimal("SEQUENCIA")
                            , importacaoPlanilhaITE.asBigDecimal("NUMLOTE"), importacaoPlanilhaITE.asBigDecimal("CODCTACTB")});

            DynamicVO usuarioVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

            // Verificando se foi gerado os lançamentos contabeis

            if( lancamentosContabeisVO == null
                    & ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                ErroUtils.disparaErro("Lançamentos importados, não é permitido alterar! Fineza verificar!");
            }
        }
    }
}
