package br.com.mgs.importacaoPlanilhaLoteContabil;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class EventoItem implements EventoProgramavelJava {

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        ValidacoesImportacao.validaAtualizacaoImportacaoPlanilhaLoteContabilItem ( persistenceEvent );
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        ValidacoesImportacao.validaExclusaoImportacaoPlanilhaLoteContabilItem ( persistenceEvent );
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception{
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
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
