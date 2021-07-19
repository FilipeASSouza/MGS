package br.com.mgs.anexos;

import br.com.mgs.utils.ErroUtils;
import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

public class RegrasAnexoContabilidade {

    private static String numeroUnico;
    private static JapeWrapper importacaoPlanilhaITEDAO = JapeFactory.dao("AD_TCBIMPMANITE"); //AD_TCBIMPMANITE
    private static JapeWrapper mestreLotesDAO = JapeFactory.dao("MestreLote"); //TCBLOT
    private static JapeWrapper usuariosDAO = JapeFactory.dao("Usuario"); // TSIUSU

    public static void validarAnexoImportacaoPlanilhaLotesContabeis(PersistenceEvent persistenceEvent) throws Exception{

        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        numeroUnico = vo.asString("PKREGISTRO").replace(String.valueOf("_AD_TCBIMPMANCAB"), String.valueOf(""));

        if(vo.asString("NOMEINSTANCIA").equals("AD_TCBIMPMANITE")){
            ErroUtils.disparaErro("Anexo não pode ser inserido nesta tela, fineza verificar!");
        }

        if( vo.asString("NOMEINSTANCIA").equalsIgnoreCase(String.valueOf("AD_TCBIMPMANCAB")) ){

            Collection<DynamicVO> importacaoPlanilhaITENSVO = importacaoPlanilhaITEDAO.find("NUIMPMAN = ?", new Object[]{ numeroUnico });
            Iterator lancamentos = importacaoPlanilhaITENSVO.iterator();

            while( lancamentos.hasNext() ){

                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");

                DynamicVO importacaoPlanilhaITE = (DynamicVO) lancamentos.next();
                BigDecimal numeroDocumento = importacaoPlanilhaITE.asBigDecimal("DOCUMENTO");
                BigDecimal sequencia = importacaoPlanilhaITE.asBigDecimal("SEQUENCIA");
                BigDecimal numeroLote = importacaoPlanilhaITE.asBigDecimal("NUMLOTE");
                BigDecimal codigoContaContabil = importacaoPlanilhaITE.asBigDecimal("CODCTACTB");
                String dataReferencia = sdf.format(importacaoPlanilhaITE.asTimestamp("DTREF"));

                NativeSqlDecorator verificarContabilizacaoSQL = new NativeSqlDecorator("SELECT TRUNC( REFERENCIA ) AS REFERENCIA FROM TCBLAN LAN " +
                        " WHERE LAN.NUMDOC = :NUMDOC " +
                        " AND LAN.SEQUENCIA = :SEQUENCIA" +
                        " AND LAN.NUMLOTE = :NUMLOTE" +
                        " AND LAN.CODCTACTB = :CODCTACTB" +
                        " AND LAN.REFERENCIA = ( '01' || TO_CHAR( TO_DATE(:REFERENCIA ), 'MMYYYY' ) ) ");
                verificarContabilizacaoSQL.setParametro("NUMDOC", numeroDocumento);
                verificarContabilizacaoSQL.setParametro("SEQUENCIA", sequencia);
                verificarContabilizacaoSQL.setParametro("NUMLOTE", numeroLote);
                verificarContabilizacaoSQL.setParametro("CODCTACTB", codigoContaContabil);
                verificarContabilizacaoSQL.setParametro("REFERENCIA", dataReferencia);

                Timestamp dataCompetencia = null;

                if( verificarContabilizacaoSQL.proximo() ){
                    dataCompetencia = verificarContabilizacaoSQL.getValorTimestamp("REFERENCIA");
                }

                // Verificando a situação do lote e a data de competencia "Fechado"

                if(dataCompetencia != null){

                    DynamicVO mestreLoteVO = mestreLotesDAO.findOne("CODEMP = ? AND REFERENCIA = ? AND NUMLOTE = ? "
                            , new Object[]{ importacaoPlanilhaITE.asBigDecimal("CODEMP"), dataCompetencia
                            , importacaoPlanilhaITE.asBigDecimal("NUMLOTE")});

                    DynamicVO usuarioVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

                    if( mestreLoteVO != null && mestreLoteVO.asString("SITUACAO").equalsIgnoreCase(String.valueOf("F"))
                            & ( usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL") == null || usuarioVO.asString("AD_LIBEXCLUIANEXOCONTABIL").equalsIgnoreCase(String.valueOf("N"))) ){
                        ErroUtils.disparaErro("Periodo contabil fechado, fineza verificar!");
                    }
                }
            }
        }
    }
}
