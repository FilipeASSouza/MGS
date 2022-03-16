package br.com.mgs.bloqueioRequisicao;

import br.com.mgs.utils.BuscaDadosFuncionario;
import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

public class EventoEmpregadoDemitido implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabecalhoVO = (DynamicVO) persistenceEvent.getVo();
        BigDecimal codigoTipoOperacao = cabecalhoVO.asBigDecimalOrZero("CODTIPOPER");
        String situacao = cabecalhoVO.asString("AD_SITUACAO");

        if( codigoTipoOperacao.equals(BigDecimal.valueOf(303L))
                || codigoTipoOperacao.equals(String.valueOf("328"))
                && ( situacao == null || situacao.isEmpty() ) ){
            new BuscaDadosFuncionario().executar(persistenceEvent);
        }

        if( cabecalhoVO.asString("TIPMOV").equalsIgnoreCase(String.valueOf("J"))
                && ( cabecalhoVO.asString("AD_SITUACAO") != null && !cabecalhoVO.asString("AD_SITUACAO").isEmpty() )
                    && cabecalhoVO.asString("AD_SITUACAO").equals(String.valueOf("Demitido")) ){
            ErroUtils.disparaErro("Empregado pertencente a matricula: " + cabecalhoVO.asString("AD_MATRICULA") + " não pertence mais ao grupo da empresa, fineza verificar!");
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabecalhoVO = (DynamicVO) persistenceEvent.getVo();

        if( persistenceEvent.getModifingFields().isModifing("AD_MATRICULA") ){
            if( cabecalhoVO.asString("TIPMOV").equalsIgnoreCase(String.valueOf("J"))
                    && ( cabecalhoVO.asString("AD_SITUACAO") != null && !cabecalhoVO.asString("AD_SITUACAO").isEmpty() )
                    && cabecalhoVO.asString("AD_SITUACAO").equals(String.valueOf("Demitido")) ){
                ErroUtils.disparaErro("Empregado pertencente a matricula: "+ cabecalhoVO.asString("AD_MATRICULA") + " não pertence mais ao grupo da empresa, fineza verificar!");
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) { }

    @Override
    public void beforeCommit(TransactionContext transactionContext) { }
}
