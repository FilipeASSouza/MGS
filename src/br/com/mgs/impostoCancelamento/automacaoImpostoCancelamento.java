package br.com.mgs.impostoCancelamento;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;

import java.math.BigDecimal;

public class automacaoImpostoCancelamento implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) {}

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        Boolean confirmando = (Boolean) JapeSession.getProperty("CabecalhoNota.confirmando.nota");
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        if( vo.asBigDecimal("CODTIPOPER").equals(BigDecimal.valueOf(900L))
            && confirmando != null ){
            DynamicVO notaOrigemVO = JapeFactory.dao("CompraVendavariosPedido").findOne("NUNOTA = ?", new Object[]{vo.asBigDecimal("NUNOTA")});
            if( notaOrigemVO != null){
                DynamicVO notaVO = JapeFactory.dao("CabecalhoNota").findByPK(notaOrigemVO.asBigDecimal("NUNOTAORIG"));
                vo.setProperty("BASEISS", notaVO.asBigDecimalOrZero("BASEISS"));
                vo.setProperty("VLRISS", notaVO.asBigDecimalOrZero("VLRISS"));
                vo.setProperty("ISSRETIDO", notaVO.asString("ISSRETIDO"));
            }
        }
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
