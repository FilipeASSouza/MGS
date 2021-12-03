package br.com.mgs.diariasDeViagem.agendamento;

import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ReintegrarRateio {

    private JapeWrapper rateioDAO = JapeFactory.dao("RateioRecDesp");
    private JapeWrapper financeiroDAO = JapeFactory.dao("Financeiro");

    public void processar(JdbcWrapper jdbcWrapper) throws Exception{

        NativeSqlDecorator consultaDiariasSemRateioSQL = new NativeSqlDecorator(this,"diariaSemRateio.sql" );
        BigDecimal numeroFinanceiro = BigDecimal.ZERO;

        try{

            while(consultaDiariasSemRateioSQL.proximo() ){

                String origem = consultaDiariasSemRateioSQL.getValorString("ORIGEM");
                numeroFinanceiro = consultaDiariasSemRateioSQL.getValorBigDecimal("NUFIN");
                BigDecimal natureza = consultaDiariasSemRateioSQL.getValorBigDecimal("CODNAT");
                BigDecimal centroResultado = consultaDiariasSemRateioSQL.getValorBigDecimal("CODCENCUS");
                BigDecimal projeto = consultaDiariasSemRateioSQL.getValorBigDecimal("CODPROJ");
                BigDecimal percentualRateio = consultaDiariasSemRateioSQL.getValorBigDecimal("PERCRATEIO");
                BigDecimal contaContabil = consultaDiariasSemRateioSQL.getValorBigDecimal("CODCTACTB");
                BigDecimal unidade = consultaDiariasSemRateioSQL.getValorBigDecimal("CODSITE");
                BigDecimal parceiro = consultaDiariasSemRateioSQL.getValorBigDecimal("CODPARC");
                Timestamp dataAlteracao = consultaDiariasSemRateioSQL.getValorTimestamp("DTALTER");

                FluidCreateVO rateioFCVO = rateioDAO.create();
                rateioFCVO.set("ORIGEM", origem );
                rateioFCVO.set("NUFIN", numeroFinanceiro );
                rateioFCVO.set("CODNAT", natureza );
                rateioFCVO.set("CODCENCUS", centroResultado );
                rateioFCVO.set("CODPROJ", projeto );
                rateioFCVO.set("PERCRATEIO", percentualRateio );
                rateioFCVO.set("CODCTACTB", contaContabil );
                rateioFCVO.set("NUMCONTRATO", BigDecimal.ZERO );
                rateioFCVO.set("DIGITADO", String.valueOf("N") );
                rateioFCVO.set("CODSITE", unidade );
                rateioFCVO.set("CODPARC", parceiro );
                rateioFCVO.set("CODUSU", BigDecimal.ZERO );
                rateioFCVO.set("DTALTER", dataAlteracao );
                rateioFCVO.save();

            }

            NativeSqlDecorator atualizandoFinanceiroSQL = new NativeSqlDecorator("UPDATE TGFFIN" +
                    " SET RATEADO = 'S'" +
                    " WHERE NUFIN = :NUFIN ", jdbcWrapper );
            atualizandoFinanceiroSQL.setParametro("NUFIN", numeroFinanceiro );
            atualizandoFinanceiroSQL.atualizar();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            consultaDiariasSemRateioSQL.close();
        }
    }
}
