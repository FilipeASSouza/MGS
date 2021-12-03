package br.com.mgs.solicitacaoTransporte.listenner;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

public class DisparoEmail implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        BigDecimal codigoUsuario = AuthenticationInfo.getCurrent().getUserID();

        DynamicVO usuarioVO = JapeFactory.dao("Usuario").findByPK(codigoUsuario);
        JapeWrapper filaDAO = JapeFactory.dao("MSDFilaMensagem");
        JapeWrapper controleNumeracaoDAO = JapeFactory.dao("ControleNumeracao");
        Timestamp dataSolicitacao = new Timestamp(TimeUtils.getToday());
        String mensagem = "Solicitacao de Transporte Criada: <br \\>" +
                "<br \\>" +
                "Solicitante: " + usuarioVO.asString("AD_APRESENTACAO") +
                "<br \\> Número do Atendimento: " + vo.asBigDecimalOrZero("NUSOL").toString() +
                "<br \\> Data e Hora da Solicitacao: " + dataSolicitacao.toString() +
                "<br \\> Valor do Voucher: " + vo.asBigDecimalOrZero("VLRVOUCHER").toString() +
                "<br \\> Observação: " + vo.asString("OBSERVACAO");

        char[] texto = new char[mensagem.length()];
        for (int i = 0; i < mensagem.length(); i++) {
            texto[i] = mensagem.charAt(i);
        }

        Collection <DynamicVO> emailsCadastradoVO = JapeFactory.dao("AD_EMAILSOLTRANS").find("1=1");

        for(DynamicVO emailCadastradoVO : emailsCadastradoVO ){

            DynamicVO ultimoNumeroVO = controleNumeracaoDAO.findOne("ARQUIVO = ?"
                    , new Object[]{String.valueOf("TMDFMG")});

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
            filaFCVO.set("ASSUNTO", "Solicitacao de Transporte - Atendimento " + vo.asBigDecimalOrZero("NUSOL") );
            filaFCVO.set("EMAIL", emailCadastradoVO.asString("EMAIL") );
            filaFCVO.set("CODUSU", codigoUsuario );
            filaFCVO.set("REENVIAR", "N" );
            filaFCVO.set("CODSMTP", BigDecimal.valueOf(5L) );
            filaFCVO.set("DHULTTENTA", dataSolicitacao );
            filaFCVO.set("DBHASHCODE", "29c0e113827a441024f5c71836fdd6eaea9b9410" );
            filaFCVO.save();

            FluidUpdateVO controleNumeracaoFUVO = controleNumeracaoDAO.prepareToUpdate( ultimoNumeroVO );
            controleNumeracaoFUVO.set("ULTCOD", codigoFila );
            controleNumeracaoFUVO.update();
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

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
