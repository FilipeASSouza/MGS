package br.com.mgs.diariasDeViagem.agendamento;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ReintegrarRateioJob implements ScheduledAction {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        ReintegrarRateio reintegrarRateio = new ReintegrarRateio();

        try{
            setup();
            reintegrarRateio.processar(jdbcWrapper);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setup() {
        JapeSessionContext.putProperty("usuario_logado", BigDecimal.ZERO);
        JapeSessionContext.putProperty("emp_usu_logado", BigDecimal.ZERO);
        JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
        JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
    }
}
