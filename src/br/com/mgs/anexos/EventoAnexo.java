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

import java.text.SimpleDateFormat;

public class EventoAnexo implements EventoProgramavelJava {

    private String numeroUnicoFinanceiroOrig;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO)persistenceEvent.getVo();
        this.numeroUnicoFinanceiroOrig = vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf(""));
        if (vo.asString("NOMEINSTANCIA").equalsIgnoreCase(String.valueOf("Financeiro"))) {
            DynamicVO financeiroVO = JapeFactory.dao("Financeiro").findOne("NUFIN = ?", new Object[]{this.numeroUnicoFinanceiroOrig});
            if (financeiroVO.asBigDecimal("NUNOTA") != null) {
                if( this.sdf.format(financeiroVO.asTimestamp("DTVENC")).compareTo(this.sdf.format(TimeUtils.getNow())) < 0 ){
                    DynamicVO usuarioVO = JapeFactory.dao("Usuario").findByPK(new Object[]{vo.asBigDecimal("CODUSU")});
                    if (usuarioVO.asString("AD_LIBEXCLUIANEXO") != null
                            && usuarioVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N"))) {
                        ErroUtils.disparaErro("Titulo vencido, usuário não possui permissão para anexar! ");
                    }
                    if ( BloqueioAnexoController.validaReferencia(financeiroVO.asBigDecimal("NUFIN"))
                            && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                            || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equals(String.valueOf("N"))) ) {
                        ErroUtils.disparaErro("Periodo contábil fechado, não pode ser incluir anexo ! ");
                    }
                }
            }
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO)persistenceEvent.getVo();
        this.numeroUnicoFinanceiroOrig = vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf(""));
        if (vo.asString("NOMEINSTANCIA").equalsIgnoreCase(String.valueOf("Financeiro"))) {
            DynamicVO financeiroVO = JapeFactory.dao("Financeiro").findOne("NUFIN = ?", new Object[]{this.numeroUnicoFinanceiroOrig});
            if (financeiroVO.asBigDecimal("NUNOTA") != null ) {
                if( this.sdf.format(financeiroVO.asTimestamp("DTVENC")).compareTo( this.sdf.format(TimeUtils.getNow()) ) < 0 ){
                    DynamicVO usuarioVO = JapeFactory.dao("Usuario").findByPK(new Object[]{AuthenticationInfo.getCurrent().getUserID()});
                    if (usuarioVO.asString("AD_LIBEXCLUIANEXO") != null
                            && usuarioVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N"))) {
                        ErroUtils.disparaErro("Titulo vencido, usuário sem permissão para alterar anexo! ");
                    }
                    if( BloqueioAnexoController.validaReferencia(financeiroVO.asBigDecimal("NUFIN"))
                            && ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                            || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equals(String.valueOf("N"))) ) {
                        ErroUtils.disparaErro("Periodo contábil fechado, anexo não pode ser alterado ! ");
                    }
                }
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO)persistenceEvent.getVo();
        this.numeroUnicoFinanceiroOrig = vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf(""));
        if (vo.asString("NOMEINSTANCIA").equalsIgnoreCase(String.valueOf("Financeiro"))) {
            DynamicVO financeiroVO = JapeFactory.dao("Financeiro").findOne("NUFIN = ?", new Object[]{this.numeroUnicoFinanceiroOrig});
            if (financeiroVO.asBigDecimal("NUNOTA") != null) {
                JapeWrapper anexoApoioDAO = JapeFactory.dao("AD_ANEXOTSIATAETSIANX");
                JapeWrapper anexoDAO = JapeFactory.dao("Anexo");
                DynamicVO usuarioVO;
                DynamicVO anexoApoioVO;
                if ( this.sdf.format(financeiroVO.asTimestamp("DTVENC")).compareTo(this.sdf.format(TimeUtils.getNow()) ) < 0 ) {
                    usuarioVO = JapeFactory.dao("Usuario").findByPK(new Object[]{AuthenticationInfo.getCurrent().getUserID()});
                    if ( usuarioVO.asString("AD_LIBEXCLUIANEXO") != null
                            && usuarioVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N"))) {
                        ErroUtils.disparaErro("Titulo vencido, usuário não possui permissão para excluir! ");
                    }

                    if ( BloqueioAnexoController.validaReferencia(financeiroVO.asBigDecimal("NUFIN")) ) {
                        if( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null
                                || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equals(String.valueOf("N")) ){
                            ErroUtils.disparaErro("Periodo contábil fechado, não pode excluir anexo ! ");
                        }
                        anexoApoioVO = anexoApoioDAO.findOne("NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                        if (anexoApoioVO != null) {
                            anexoApoioDAO.delete(new Object[]{anexoApoioVO.asBigDecimal("NUATTACH")});
                            DynamicVO anexoVO = anexoDAO.findOne("AD_NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                            if (anexoVO != null) {
                                anexoDAO.delete(new Object[]{anexoVO.asBigDecimal("CODATA"), anexoVO.asString("TIPO"), anexoVO.asString("DESCRICAO"), anexoVO.asBigDecimal("SEQUENCIA"), anexoVO.asBigDecimal("SEQUENCIAPR")});
                            }
                        }
                    }

                    anexoApoioVO = anexoApoioDAO.findOne("NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                    if (anexoApoioVO != null) {
                        anexoApoioDAO.delete(new Object[]{anexoApoioVO.asBigDecimal("NUATTACH")});
                        DynamicVO anexoVO = anexoDAO.findOne("AD_NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                        if (anexoVO != null) {
                            anexoDAO.delete(new Object[]{anexoVO.asBigDecimal("CODATA"), anexoVO.asString("TIPO"), anexoVO.asString("DESCRICAO"), anexoVO.asBigDecimal("SEQUENCIA"), anexoVO.asBigDecimal("SEQUENCIAPR")});
                        }
                    }
                }

                usuarioVO = anexoApoioDAO.findOne("NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                if (usuarioVO != null) {
                    anexoApoioDAO.delete(new Object[]{usuarioVO.asBigDecimal("NUATTACH")});
                    anexoApoioVO = anexoDAO.findOne("AD_NUATTACH = ?", new Object[]{vo.asBigDecimal("NUATTACH")});
                    if (anexoApoioVO != null) {
                        anexoDAO.delete(new Object[]{anexoApoioVO.asBigDecimal("CODATA"), anexoApoioVO.asString("TIPO"), anexoApoioVO.asString("DESCRICAO"), anexoApoioVO.asBigDecimal("SEQUENCIA"), anexoApoioVO.asBigDecimal("SEQUENCIAPR")});
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
    public void beforeCommit(TransactionContext transactionContext) {    }
}
