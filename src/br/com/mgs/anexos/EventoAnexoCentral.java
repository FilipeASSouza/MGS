package br.com.mgs.anexos;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class EventoAnexoCentral implements EventoProgramavelJava {

    private DynamicVO vo = null;
    private DynamicVO financeiroVO = null;
    private DynamicVO usuarioVO = null;
    private final JapeWrapper financeiroDAO = JapeFactory.dao("Financeiro");
    private final JapeWrapper usuarioDAO = JapeFactory.dao("Usuario");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        this.vo = (DynamicVO)persistenceEvent.getVo();
        if (!this.vo.asBigDecimal("AD_CODUSUJOB").equals(BigDecimal.ZERO)) {
            this.vo.setProperty("AD_TIPINCLUSAO", String.valueOf(0));
        } else if ( vo.asString("AD_TIPINCLUSAO") != null
                && this.vo.asString("AD_TIPINCLUSAO").equalsIgnoreCase(String.valueOf(1))) {
            this.financeiroVO = this.financeiroDAO.findOne("NUNOTA = ?", new Object[]{this.vo.asBigDecimal("CODATA")});
            if (BloqueioAnexoController.validaReferencia(this.financeiroVO.asBigDecimal("NUFIN"))) {
                ErroUtils.disparaErro("Periodo contábil fechado, anexo não pode ser alterado ! ");
            } else if (this.usuarioVO.asString("AD_LIBEXCLUIANEXO") == null || this.usuarioVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N"))) {
                ErroUtils.disparaErro("Titulo vencido, usuário sem permissão para alterar anexo! ");
            }
        }
    }


    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception{
        this.vo = (DynamicVO)persistenceEvent.getVo();

        if (vo.asString("AD_TIPINCLUSAO") != null
                && this.vo.asString("AD_TIPINCLUSAO").equalsIgnoreCase(String.valueOf(1))) {
            this.financeiroVO = this.financeiroDAO.findOne("NUNOTA = ?", new Object[]{this.vo.asBigDecimal("CODATA")});
            this.usuarioVO = this.usuarioDAO.findByPK(new Object[]{AuthenticationInfo.getCurrent().getUserID()});
            if ( this.sdf.format(financeiroVO.asTimestamp("DTVENC")).compareTo(this.sdf.format(TimeUtils.getNow()) ) < 0 ) {
                if ( this.usuarioVO.asString("AD_LIBEXCLUIANEXO") != null
                        && this.usuarioVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N")) ) {
                    ErroUtils.disparaErro("Usuário sem permissão para deletar anexo! ");
                }
                if ( BloqueioAnexoController.validaReferencia(this.financeiroVO.asBigDecimal("NUFIN")) ) {
                    if( ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                            || this.usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equals(String.valueOf("N"))) ){
                        ErroUtils.disparaErro("Periodo contábil fechado, anexo não pode ser deletado ! ");
                    }
                }
            }
        }
    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) {    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) {    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) {  }
}
