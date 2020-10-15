import br.com.sankhya.bh.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class validadatarefEvento implements EventoProgramavelJava {

    public validadatarefEvento(){}

    public void validaReferencia(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO itemVO = (DynamicVO)persistenceEvent.getVo();
        JapeWrapper empDAO = JapeFactory.dao("EmpresaContabilidade");
        DynamicVO empVO = empDAO.findOne("CODEMP = ?", new Object[]{BigDecimal.ONE});
        if (empVO.asTimestamp("REFERENCIA").compareTo(itemVO.asTimestamp("DTREF")) > 0) {
            ErroUtils.disparaErro("Periodo Contabil Fechado para essa data de Referencia");
        }

    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        this.validaReferencia(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        this.validaReferencia(persistenceEvent);
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
