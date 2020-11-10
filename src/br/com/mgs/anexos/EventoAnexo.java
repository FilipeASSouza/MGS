package br.com.mgs.anexos;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

public class EventoAnexo implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        if( vo.asString("NOMEINSTANCIA").equals(String.valueOf("Financeiro")) && vo.asString("AD_NUFIN") == null ){
            vo.setProperty("AD_NUFIN", vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf("")) );
        }

        DynamicVO finVO = JapeFactory.dao("Financeiro").findOne("NUFIN = ?"
            , new Object[]{vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf(""))});

        if( finVO.asTimestamp("DTVENC").compareTo( TimeUtils.getNow() ) < 0 ){
            DynamicVO usuVO = JapeFactory.dao("Usuario").findByPK(AuthenticationInfo.getCurrent().getUserID());
            if(usuVO.asString("AD_LIBEXCLUIANEXO") == null
                || usuVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N")) ){
                ErroUtils.disparaErro("Titulo vencido, usuário não possui permissão para anexar! ");
            } else if( BloqueioAnexoController.validaReferencia(finVO.asBigDecimal("NUFIN")) ){
                ErroUtils.disparaErro("Periodo contábil fechado, não pode ser inclui anexo ! ");
            }
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        DynamicVO finVO = JapeFactory.dao("Financeiro").findOne( "NUFIN = ?"
                , new Object[]{ vo.asString("AD_NUFIN") });

        if( finVO.asTimestamp("DTVENC").compareTo( TimeUtils.getNow() ) < 0 ) {

            DynamicVO usuVO = JapeFactory.dao("Usuario").findByPK(AuthenticationInfo.getCurrent().getUserID());

            if( usuVO.asString("AD_LIBEXCLUIANEXO") == null
                    || usuVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N")) ){
                ErroUtils.disparaErro("Titulo vencido, usuário sem permissão para alterar anexo! ");
            }else if( BloqueioAnexoController.validaReferencia(finVO.asBigDecimal("NUFIN")) ){
                ErroUtils.disparaErro("Periodo contábil fechado, anexo não pode ser alterado ! ");
            }
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        DynamicVO finVO = JapeFactory.dao("Financeiro").findOne("NUFIN = ?"
                , new Object[]{vo.asString("PKREGISTRO").replace(String.valueOf("_Financeiro"), String.valueOf(""))});

        if( finVO.asTimestamp("DTVENC").compareTo( TimeUtils.getNow() ) < 0 ){
            DynamicVO usuVO = JapeFactory.dao("Usuario").findByPK(AuthenticationInfo.getCurrent().getUserID());
            if(usuVO.asString("AD_LIBEXCLUIANEXO") == null
                    || usuVO.asString("AD_LIBEXCLUIANEXO").equals(String.valueOf("N")) ){
                ErroUtils.disparaErro("Titulo vencido, usuário não possui permissão para excluir! ");
            }else if( BloqueioAnexoController.validaReferencia(finVO.asBigDecimal("NUFIN")) ){
                ErroUtils.disparaErro("Periodo contábil fechado, não pode excluir anexo ! ");
            }
        }
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
