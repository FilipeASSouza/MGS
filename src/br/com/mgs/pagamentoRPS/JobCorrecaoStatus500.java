package br.com.mgs.pagamentoRPS;

import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class JobCorrecaoStatus500 implements ScheduledAction {

    private NativeSqlDecorator listaPendencias = null;
    private JapeWrapper filaIntegracaoDAO = JapeFactory.dao("TSIFILINT");
    private BigDecimal numeroFilIntegracao;
    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {
        try{
            setup();
            processar();
        }catch(IllegalStateException i){
            i.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        JapeSessionContext.putProperty("usuario_logado", BigDecimal.ZERO);
        JapeSessionContext.putProperty("emp_usu_logado", (Object)null);
        JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
        JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
    }

    private void processar() throws Exception{
        carregarListaProcessamento();
        percorrerListaProcessamento();
    }

    private void carregarListaProcessamento() throws Exception{
        listaPendencias = new NativeSqlDecorator(this, "consultarPendencias.sql" );
    }

    private void percorrerListaProcessamento() throws Exception{
        while(listaPendencias.proximo()){
            numeroFilIntegracao = listaPendencias.getValorBigDecimal("NUFILAINTEGRACAO");
            atualizarRegistros();
        }
    }

    private void atualizarRegistros() throws Exception{

        try {

            DynamicVO filaIntegracaoVO = filaIntegracaoDAO.findOne("NUFILAINTEGRACAO = ?"
                    , new Object[]{numeroFilIntegracao});
            FluidUpdateVO filaIntegracaoFUVO = filaIntegracaoDAO.prepareToUpdate(filaIntegracaoVO);
            filaIntegracaoFUVO.set("DHPROC", null);
            filaIntegracaoFUVO.set("STATUSPROC", null);
            filaIntegracaoFUVO.update();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            listaPendencias.close();
        }
    }
}
