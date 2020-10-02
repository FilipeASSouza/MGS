package br.com.mgs.restricaowms;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class EventoUpdateControle implements EventoProgramavelJava {

    public EventoUpdateControle() {
    }

    private void updateControle(PersistenceEvent persistenceEvent) throws Exception {
        JapeWrapper produtoDAO = JapeFactory.dao("Produto");
        DynamicVO tgwexpVO = (DynamicVO)persistenceEvent.getVo();
        BigDecimal codprod = tgwexpVO.asBigDecimal("CODPROD");
        DynamicVO produtoVO2 = produtoDAO.findOne("CODPROD = ?", new Object[]{codprod});
        if (!"S".equals(produtoVO2.asString("TIPCONTEST"))) {
            tgwexpVO.setProperty("AD_CONTROLE", (Object)null);
        } else {
            String controle = tgwexpVO.asString("AD_CONTROLE").trim();
            if (controle != null) {
                DynamicVO produtoVO = produtoDAO.findOne("CODPROD = ? AND LISCONTEST like '%?%'", new Object[]{codprod, controle});
                if (produtoVO == null) {
                    throw new Exception("Controle/tamanho não pertence ao produto, verifique o cadastro de produto ");
                }

                tgwexpVO.setProperty("CONTROLE", controle);
            }

        }
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        this.updateControle(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
