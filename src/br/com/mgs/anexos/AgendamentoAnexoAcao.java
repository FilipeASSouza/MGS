package br.com.mgs.anexos;

import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.TimeUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;

public class AgendamentoAnexoAcao implements AcaoRotinaJava {

    private NativeSqlDecorator listaPendenciasNSD = null;
    private BigDecimal numeroUnicoAnexoOrigem;
    private String chaveArquivo;
    private File file;
    private BigDecimal numeroUnicoAnexoDestino;
    private String nomeArquivoOrigem;
    private BigDecimal numeroUnicoNota;
    private String descricaoArquivo;
    private JapeWrapper apoioAnexoLogDAO = JapeFactory.dao("AD_ANEXOTSIATAETSIANXLOG");

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        try {
            processar();
        } catch (Exception e) {
            FluidCreateVO apoioAnexoLogFCVO = apoioAnexoLogDAO.create();
            apoioAnexoLogFCVO.set("NUATTACH", numeroUnicoAnexoOrigem );
            apoioAnexoLogFCVO.set("STATUS", e.getMessage() + " Nufin: " + nomeArquivoOrigem.substring(0,7) );
            try {
                apoioAnexoLogFCVO.save();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void processar() throws Exception {
        carregarBaseProcessamento();
        percorrerlistadePendencias();
    }


    private void carregarBaseProcessamento() throws Exception {
        listaPendenciasNSD = new NativeSqlDecorator(this, "queAgendamentoAnexoBase.sql");
    }

    private void percorrerlistadePendencias() throws Exception {
        while(listaPendenciasNSD.proximo()){
            numeroUnicoAnexoOrigem = listaPendenciasNSD.getValorBigDecimal("NUATTACH");
            copiarAnexo();
        }
    }

    private void copiarAnexo() throws Exception {
        carregarDadosAnexo();
        carregarArquivo();
        criaAnexoDesitno();
        registraVinculoAnexos();
    }

    private void registraVinculoAnexos() throws Exception {
        JapeWrapper ad_linkanexoDAO = JapeFactory.dao("AD_ANEXOTSIATAETSIANX");
        FluidCreateVO ad_linkanexoFCVO = ad_linkanexoDAO.create();
        ad_linkanexoFCVO.set("NUATTACH", numeroUnicoAnexoOrigem);
        ad_linkanexoFCVO.set("CODATA", numeroUnicoAnexoDestino);
        ad_linkanexoFCVO.save();
    }

    private void carregarArquivo() {
        String diretorioBase = SWRepositoryUtils.getBaseFolder().getPath();
        String caminhoBase = diretorioBase+"/Sistema/Anexos/Financeiro/";
        String caminhoArquivo = caminhoBase+chaveArquivo;
        file = new File(caminhoArquivo);
    }

    private void criaAnexoDesitno() throws Exception {

        if( nomeArquivoOrigem != null ){

            JapeWrapper anexoDAO = JapeFactory.dao("Anexo");
            FluidCreateVO anexoFCVO = anexoDAO.create();
            anexoFCVO.set("DTALTER", TimeUtils.getNow());
            anexoFCVO.set("EDITA","N");
            anexoFCVO.set("ARQUIVO",  nomeArquivoOrigem );
            anexoFCVO.set("DESCRICAO", descricaoArquivo.length() > 40 ? descricaoArquivo.substring(0, 40) : descricaoArquivo );
            anexoFCVO.set("TIPOCONTEUDO","P");
            anexoFCVO.set("TIPO","N");
            anexoFCVO.set("CODUSU", BigDecimal.ZERO);
            anexoFCVO.set("CONTEUDO", FileUtils.readFileToByteArray(file));
            anexoFCVO.set("PUBLICO","N");
            anexoFCVO.set("SEQUENCIAPR", BigDecimal.ZERO );
            anexoFCVO.set("SEQUENCIA", BigDecimal.ZERO );
            anexoFCVO.set("DTINCLUSAO",TimeUtils.getNow() );
            anexoFCVO.set("CODATA", numeroUnicoNota );
            anexoFCVO.set("AD_TIPINCLUSAO", String.valueOf(1) );
            anexoFCVO.set("AD_NUATTACH", numeroUnicoAnexoOrigem );
            anexoFCVO.set("AD_CODUSUJOB", BigDecimal.ZERO );

            DynamicVO save = anexoFCVO.save();
            numeroUnicoAnexoDestino = save.asBigDecimal("CODATA");

        }
    }

    private void carregarDadosAnexo() throws Exception {
        JapeWrapper anexoSistemaDAO = JapeFactory.dao("AnexoSistema");
        DynamicVO anexoSistemaVO = anexoSistemaDAO.findByPK(numeroUnicoAnexoOrigem);
        DynamicVO finVO = JapeFactory.dao("Financeiro").findByPK(anexoSistemaVO.asString("PKREGISTRO").replace("_Financeiro",""));
        chaveArquivo = anexoSistemaVO.asString("CHAVEARQUIVO");
        nomeArquivoOrigem = anexoSistemaVO.asString("NOMEARQUIVO");
        numeroUnicoNota = finVO.asBigDecimal("NUNOTA");
        descricaoArquivo = anexoSistemaVO.asString("DESCRICAO");
    }
}
