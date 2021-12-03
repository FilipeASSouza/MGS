package br.com.mgs.filaIntegracao;

import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class EventoFilaIntegracao implements EventoProgramavelJava {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();

    public Boolean verificarFatura(BigDecimal numeroFatura) throws Exception{

        boolean status = false;
        //Verifica se a fatura existe no sistema novo

        NativeSqlDecorator consultandoFaturaSQL = new NativeSqlDecorator("SELECT COUNT(*) QTD" +
                " FROM MGSTCTFATURA WHERE NUFATURA = :NUFATURA", jdbcWrapper );
        consultandoFaturaSQL.setParametro("NUFATURA", numeroFatura );

        try{
            if(consultandoFaturaSQL.proximo() ){
                if( !consultandoFaturaSQL.getValorBigDecimal("QTD").equals(BigDecimal.ZERO) ){
                    status = true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            consultandoFaturaSQL.close();
        }

        return status;
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        String nomeInstancia = persistenceEvent.getEntity().getDescription();
        DynamicVO dynamicVO = (DynamicVO)persistenceEvent.getVo();
        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        if (modifingFields.containsKey("DHBAIXA")
                && ( verificarFatura(dynamicVO.asBigDecimalOrZero("NUMNOTA")) //somente se for verdadeiro
                || dynamicVO.asBigDecimal("AD_DIARIA") != null)) {
            String operacao = modifingFields.getNewValue("DHBAIXA") != null ? "BAIXA" : "ESTORNO";
            BigDecimal top;
            if (modifingFields.containsKey("CODTIPOPERBAIXA")) {
                if (modifingFields.getNewValue("DHBAIXA") == null) {
                    top = (BigDecimal)modifingFields.getOldValue("CODTIPOPERBAIXA");
                } else {
                    top = (BigDecimal)modifingFields.getNewValue("CODTIPOPERBAIXA");
                }
            } else {
                top = dynamicVO.asBigDecimal("CODTIPOPERBAIXA");
            }

            FilaController.setFilaIntegracao(nomeInstancia, dynamicVO, operacao, top);
        }
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
