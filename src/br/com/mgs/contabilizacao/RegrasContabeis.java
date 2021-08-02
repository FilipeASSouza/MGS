package br.com.mgs.contabilizacao;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

public class RegrasContabeis {

    public void permissaoAlteracao(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getOldVO();

        if( vo.asString("SITUACAO").equalsIgnoreCase("F") ){
            DynamicVO permissaoUsuario = JapeFactory.dao("Usuario").findByPK(AuthenticationInfo.getCurrent().getUserID());

            if(permissaoUsuario.asString("AD_PERMITEALTERARLOTE").equalsIgnoreCase("N")
                || !permissaoUsuario.asString("AD_PERMITEALTERARLOTE").equalsIgnoreCase("S") ){
                ErroUtils.disparaErro("Usuario não possui permissão para alterar o lote contábil fechado!");
            }
        }
    }

}
