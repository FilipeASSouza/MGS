package br.com.mgs.contabilizacao;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;

public class EventosContabeis implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        DynamicVO verificarCompensacao = JapeFactory.dao("Financeiro").findOne("NUCOMPENS = ?", new Object[]{vo.asBigDecimalOrZero("NUMDOC")});
        if( verificarCompensacao != null ){
            vo.setProperty("NUMDOC", verificarCompensacao.asBigDecimalOrZero("NUMNOTA"));
            vo.setProperty("COMPLHIST", vo.asString("COMPLHIST").concat(" - " + verificarCompensacao.asBigDecimal("NUMNOTA").toString()));
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {}

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) {}

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) {}

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {}

    @Override
    public void beforeCommit(TransactionContext transactionContext) {}
}
