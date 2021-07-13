package br.com.mgs.importacaoPlanilhaLoteContabil;

import br.com.mgs.utils.ErroUtils;
import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

import java.util.Collection;
import java.util.Iterator;

public class ValidacoesImportacao {

    private static JapeWrapper importacaoPlanilhaITEDAO = JapeFactory.dao("AD_TCBIMPMANITE"); //AD_TCBIMPMANITE
    private static JapeWrapper lancamentosContabeisDAO = JapeFactory.dao("Lancamento"); //TCBLAN
    private static JapeWrapper usuariosDAO = JapeFactory.dao("Usuario"); // TSIUSU

    public static void validaExclusaoImportacaoPlanilhaLoteContabilCabecalho(PersistenceEvent persistenceEvent) throws Exception{

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

            NativeSqlDecorator mestreLote = new NativeSqlDecorator("SELECT SITUACAO FROM TCBLOT WHERE CODEMP = :CODEMP " +
                            "AND TO_CHAR( REFERENCIA, 'YYYYMM' ) = :REFERENCIA AND NUMLOTE = :NUMLOTE ");
            mestreLote.setParametro("CODEMP", importacaoPlanilhaITE.asBigDecimal("CODEMP"));
            mestreLote.setParametro("REFERENCIA", TimeUtils.getYearMonth(importacaoPlanilhaITE.asTimestamp("DTREF")));
            mestreLote.setParametro("NUMLOTE", importacaoPlanilhaITE.asBigDecimal("NUMLOTE"));

            if( mestreLote.proximo() ){
                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                    && usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")) ){
                    ErroUtils.disparaErro("Periodo Contabil fechado, não é permitido excluir! Fineza verificar!");
                }

                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("A"))
                        && lancamentosContabeisVO != null
                        && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")) )){
                    ErroUtils.disparaErro("Lançamentos contabeis gerados, não é permitido excluir! Fineza verificar!");
                }
            }
        }
    }

    public static void validaExclusaoImportacaoPlanilhaLoteContabilItem (PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        DynamicVO lancamentosContabeisVO = lancamentosContabeisDAO.findOne( "NUMDOC = ? AND SEQUENCIA = ? AND NUMLOTE = ? AND CODCTACTB = ? "
                , new Object[]{ vo.asBigDecimal("DOCUMENTO"), vo.asBigDecimal("SEQUENCIA")
                        , vo.asBigDecimal("NUMLOTE"), vo.asBigDecimal("CODCTACTB") } );

        // Verificando se o usuario tem permissao para excluir os registros na importacao da planilha

        DynamicVO usuarioVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

        NativeSqlDecorator mestreLote = new NativeSqlDecorator("SELECT SITUACAO FROM TCBLOT WHERE CODEMP = :CODEMP " +
                "AND TO_CHAR( REFERENCIA, 'YYYYMM' ) = :REFERENCIA AND NUMLOTE = :NUMLOTE ");
        mestreLote.setParametro("CODEMP", vo.asBigDecimal("CODEMP"));
        mestreLote.setParametro("REFERENCIA", TimeUtils.getYearMonth(vo.asTimestamp("DTREF")));
        mestreLote.setParametro("NUMLOTE", vo.asBigDecimal("NUMLOTE"));

        if( mestreLote.proximo() ){

            if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                    || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")) )){
                    ErroUtils.disparaErro("Periodo Contabil fechado, não é permitido excluir! Fineza verificar!");
            }

            if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("A"))
                && ( lancamentosContabeisVO != null
                    && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                    || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")))) ){
                    ErroUtils.disparaErro("Lançamentos contabeis gerados, não é permitido excluir o registro! Fineza verificar!");
            }
        }
    }

    public static void validaAtualizacaoImportacaoPlanilhaLoteContabilCabecalho (PersistenceEvent persistenceEvent) throws Exception{

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

            NativeSqlDecorator mestreLote = new NativeSqlDecorator("SELECT SITUACAO FROM TCBLOT WHERE CODEMP = :CODEMP " +
                    "AND TO_CHAR( REFERENCIA, 'YYYYMM' ) = :REFERENCIA AND NUMLOTE = :NUMLOTE ");
            mestreLote.setParametro("CODEMP", vo.asBigDecimal("CODEMP"));
            mestreLote.setParametro("REFERENCIA", TimeUtils.getYearMonth(vo.asTimestamp("DTREF")));
            mestreLote.setParametro("NUMLOTE", vo.asBigDecimal("NUMLOTE"));

            if( mestreLote.proximo() ){

                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                    && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                        ErroUtils.disparaErro("Periodo Contabil fechado, não é permitido alterar! Fineza verificar!");
                }

                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("A"))
                    && ( lancamentosContabeisVO != null
                        && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")) ) ) ){
                        ErroUtils.disparaErro("Lançamentos importados, não é permitido alterar! Fineza verificar!");
                }
            }
        }
    }

    public static void validaAtualizacaoImportacaoPlanilhaLoteContabilItem ( PersistenceEvent persistenceEvent ) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getModifingFields();

        DynamicVO lancamentosContabeisVO = lancamentosContabeisDAO.findOne("NUMDOC = ? AND SEQUENCIA = ? " +
                        "AND NUMLOTE = ? AND CODCTACTB = ? "
                , new Object[]{ vo.asBigDecimal("DOCUMENTO"), vo.asBigDecimal("SEQUENCIA")
                        , vo.asBigDecimal("NUMLOTE"), vo.asBigDecimal("CODCTACTB")} );

        DynamicVO usuarioVO = usuariosDAO.findByPK( AuthenticationInfo.getCurrent().getUserID() );

        if( lancamentosContabeisVO != null ){

            NativeSqlDecorator mestreLote = new NativeSqlDecorator("SELECT SITUACAO FROM TCBLOT WHERE CODEMP = :CODEMP " +
                    " AND TO_CHAR( REFERENCIA, 'YYYYMM' ) = :REFERENCIA " +
                    " AND NUMLOTE = :NUMLOTE ");
            mestreLote.setParametro("CODEMP", vo.asBigDecimal("CODEMP"));
            mestreLote.setParametro("REFERENCIA", TimeUtils.getYearMonth(vo.asTimestamp("DTREF")));
            mestreLote.setParametro("NUMLOTE", vo.asBigDecimal("NUMLOTE"));


            if( mestreLote.proximo() ){

                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                    && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                        ErroUtils.disparaErro("Periodo Contabil fechado, não é permitido alterar! Fineza verificar!");
                }

                // Verificando se foi gerado os lançamentos contabeis

                if( mestreLote.getValorString("SITUACAO").equalsIgnoreCase(String.valueOf("A"))
                    && ( lancamentosContabeisVO != null
                        && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                        || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N")))) ){
                        ErroUtils.disparaErro("Lançamentos importados, não é permitido alterar! Fineza verificar!");
                }
            }
        }
    }
}
