package br.com.mgs.disparoEmail.ControleDesconto;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class AlertaDesconto implements ScheduledAction {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();
    private JapeWrapper filaDAO = JapeFactory.dao("MSDFilaMensagem");
    private JapeWrapper controleNumeracaoDAO = JapeFactory.dao("ControleNumeracao");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {
        try{
            setup();
            processar(jdbcWrapper);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setup() {
        JapeSessionContext.putProperty("usuario_logado", BigDecimal.ZERO);
        JapeSessionContext.putProperty("emp_usu_logado", (Object)null);
        JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
        JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
    }

    public void processar(JdbcWrapper jdbcWrapper ) throws Exception{

        DynamicVO ultimoNumeroVO = controleNumeracaoDAO.findOne("ARQUIVO = ?"
                , new Object[]{String.valueOf("TMDFMG")});

        NativeSql consultarLancamentosSQL = new NativeSql(jdbcWrapper);
        consultarLancamentosSQL.appendSql("SELECT CAB.NUNOTA, CAB.NUMNOTA, CAB.TIPMOV, TO_CHAR( CAB.DTENTSAI, 'DD/MM/YYYY' ) DTENTSAI, USU.AD_APRESENTACAO\n" +
                "FROM TGFCAB CAB \n" +
                "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA\n" +
                "INNER JOIN TSIUSU USU ON CAB.CODUSUINC = USU.CODUSU\n" +
                "INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC\n" +
                "LEFT JOIN AD_TGFCABCONTREMAIL CE ON CE.NUNOTA = CAB.NUNOTA\n" +
                "WHERE CAB.TIPMOV IN( 'C', 'O' )\n" +
                "AND EXTRACT( MONTH FROM CAB.DTENTSAI ) >= EXTRACT( MONTH FROM sysdate -1 )\n" +
                "AND EXTRACT( YEAR FROM CAB.DTENTSAI ) >= EXTRACT( YEAR FROM sysdate )\n" +
                "AND ( CAB.VLRDESCTOT IS NOT NULL\n" +
                "OR ITE.VLRDESC IS NOT NULL )\n" +
                "AND ( CAB.VLRDESCTOT > 0\n" +
                "OR ITE.VLRDESC > 0 )\n" +
                "AND CE.NUNOTA IS NULL\n" +
                "ORDER BY CAB.DTENTSAI ASC");
        ResultSet consultarLancamentosRS = consultarLancamentosSQL.executeQuery();
        try{

            StringBuilder mensagem = new StringBuilder("Segue abaixo a relação de pedidos/notas lançadas com valor de desconto: <br \\>" +
                    "<br \\>");

            Timestamp dataSolicitacao = new Timestamp(TimeUtils.getToday());
            char[] texto = null;

            while( consultarLancamentosRS.next() ){

                 mensagem.append("Nro.Único: ").append(consultarLancamentosRS.getBigDecimal("NUNOTA").toString())
                         .append(", Nro.Nota: ").append(consultarLancamentosRS.getBigDecimal("NUMNOTA").toString())
                         .append(", Dt.Entrada/Saida: ").append(consultarLancamentosRS.getString("DTENTSAI"))
                         .append(", Usuário de Inclusão: ").append(consultarLancamentosRS.getString("AD_APRESENTACAO"))
                         .append("<br \\>");

                texto = new char[mensagem.length()];
                for (int i = 0; i < mensagem.length(); i++) {
                    texto[i] = mensagem.charAt(i);
                }

                NativeSql insertAD_TGFCABCONTREMAIL = new NativeSql(jdbcWrapper);
                insertAD_TGFCABCONTREMAIL.appendSql("INSERT INTO AD_TGFCABCONTREMAIL VALUES( :NUNOTA, :DATA , :ENVIADO )");
                insertAD_TGFCABCONTREMAIL.setNamedParameter("NUNOTA", consultarLancamentosRS.getBigDecimal("NUNOTA") );
                insertAD_TGFCABCONTREMAIL.setNamedParameter("DATA", dataSolicitacao );
                insertAD_TGFCABCONTREMAIL.setNamedParameter("ENVIADO", String.valueOf("S"));
                insertAD_TGFCABCONTREMAIL.executeUpdate();
            }


            BigDecimal codigoFila = ultimoNumeroVO.asBigDecimalOrZero("ULTCOD").add(BigDecimal.ONE);

            FluidCreateVO filaFCVO = filaDAO.create();
            filaFCVO.set("CODFILA", codigoFila );
            filaFCVO.set("CODMSG", null );
            filaFCVO.set("DTENTRADA", dataSolicitacao );
            filaFCVO.set("STATUS", "Pendente");
            filaFCVO.set("CODCON", BigDecimal.ZERO );
            filaFCVO.set("TENTENVIO", BigDecimal.ZERO );
            filaFCVO.set("MENSAGEM", texto );
            filaFCVO.set("TIPOENVIO", "E" );
            filaFCVO.set("MAXTENTENVIO", BigDecimal.valueOf(3L) );
            filaFCVO.set("ASSUNTO", "Notas Lançadas com Desconto: " + sdf.format( dataSolicitacao ) );
            filaFCVO.set("EMAIL", String.valueOf("classificacaocofic@mgs.srv.br") );//classificacaocofic@mgs.srv.br filipe.augusto@mgs.srv.br
            filaFCVO.set("CODUSU", BigDecimal.ZERO );
            filaFCVO.set("REENVIAR", "N" );
            filaFCVO.set("CODSMTP", BigDecimal.valueOf(6L) ); //conta de e-mail smtp
            filaFCVO.set("DHULTTENTA", dataSolicitacao );
            filaFCVO.set("DBHASHCODE", "29c0e113827a441024f5c71836fdd6eaea9b9410" );
            filaFCVO.save();


            FluidUpdateVO controleNumeracaoFUVO = controleNumeracaoDAO.prepareToUpdate( ultimoNumeroVO );
            controleNumeracaoFUVO.set("ULTCOD", codigoFila );
            controleNumeracaoFUVO.update();

            consultarLancamentosRS.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(consultarLancamentosSQL != null){
                NativeSql.releaseResources(consultarLancamentosSQL);
            }
            jdbcWrapper.closeSession();
        }
    }
}
