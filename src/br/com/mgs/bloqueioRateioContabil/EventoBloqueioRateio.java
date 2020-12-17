package br.com.mgs.bloqueioRateioContabil;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class EventoBloqueioRateio implements EventoProgramavelJava {

    JapeWrapper notaDAO = JapeFactory.dao("CabecalhoNota");
    JapeWrapper financeiroDAO = JapeFactory.dao("Financeiro");
    JapeWrapper topDAO = JapeFactory.dao("TipoOperacao");

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        topDAO.findByPK(vo.asBigDecimal("CODTIPOPER"), vo.asTimestamp("DHTIPOPER"));

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) { }

    @Override
    public void beforeCommit(TransactionContext transactionContext) { }
}
