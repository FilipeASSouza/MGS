package br.com.mgs.diariasDeViagem.acao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class ReintegrarRateioDiariaAcao implements AcaoRotinaJava {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        Registro[] linhas = contextoAcao.getLinhas();
        NativeSql integrandoRateioSQL = new NativeSql(jdbcWrapper);
        BigDecimal numeroUnicoFinanceiro = null;

        try{
            for( Registro linha : linhas ){

                numeroUnicoFinanceiro = new BigDecimal(linha.getCampo("NUFIN").toString());
                integrandoRateioSQL.appendSql("");
                integrandoRateioSQL.setNamedParameter("NUFIN", numeroUnicoFinanceiro);
                integrandoRateioSQL.executeUpdate();

            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(integrandoRateioSQL != null){
                NativeSql.releaseResources(integrandoRateioSQL);
            }
            jdbcWrapper.closeSession();
        }
    }
}
