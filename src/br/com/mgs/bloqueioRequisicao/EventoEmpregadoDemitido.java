package br.com.mgs.bloqueioRequisicao;

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

        if( cabecalhoVO.asString("TIPMOV").equalsIgnoreCase(String.valueOf("J"))
                && ( cabecalhoVO.asString("AD_SITUACAO") != null && !cabecalhoVO.asString("AD_SITUACAO").isEmpty() )
                    && cabecalhoVO.asString("AD_SITUACAO").equals(String.valueOf("Demitido")) ){
            ErroUtils.disparaErro("Empregado não pertence mais ao grupo da empresa, fineza verificar!");
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabecalhoVO = (DynamicVO) persistenceEvent.getVo();

        if( persistenceEvent.getModifingFields().isModifing("AD_MATRICULA") ){
            if( cabecalhoVO.asString("TIPMOV").equalsIgnoreCase(String.valueOf("J"))
                    && ( cabecalhoVO.asString("AD_SITUACAO") != null && !cabecalhoVO.asString("AD_SITUACAO").isEmpty() )
                    && cabecalhoVO.asString("AD_SITUACAO").equals(String.valueOf("Demitido")) ){
                ErroUtils.disparaErro("Empregado não pertence mais ao grupo da empresa, fineza verificar!");
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
