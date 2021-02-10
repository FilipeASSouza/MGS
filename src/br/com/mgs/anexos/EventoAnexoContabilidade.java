package br.com.mgs.anexos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;

import static br.com.mgs.anexos.RegrasAnexoContabilidade.validarAnexoImportacaoPlanilhaLotesContabeis;

public class EventoAnexoContabilidade implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        validarAnexoImportacaoPlanilhaLotesContabeis(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        validarAnexoImportacaoPlanilhaLotesContabeis(persistenceEvent);
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        validarAnexoImportacaoPlanilhaLotesContabeis(persistenceEvent);
    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent)  { }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) { }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) { }

    @Override
    public void beforeCommit(TransactionContext transactionContext) { }
}
