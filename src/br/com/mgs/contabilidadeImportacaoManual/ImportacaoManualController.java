import br.com.sankhya.bh.contabilidadeImportacaoManual.ImportacaoManualModel;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;
import java.util.Iterator;

public class ImportacaoManualController {

    public ImportacaoManualController() {
    }

    public void importaPlanilha(ContextoAcao contextoAcao) throws Exception {
        ImportacaoManualModel importacaoManualModel = new ImportacaoManualModel();
        Registro linha = contextoAcao.getLinhas()[0];
        BigDecimal nuImpMan = new BigDecimal(linha.getCampo("NUIMPMAN").toString());
        importacaoManualModel.setNumeroUnicoImportacaoManual(nuImpMan);
        Iterator var5 = importacaoManualModel.getListaDeArquivos("AD_TCBIMPMANCAB", nuImpMan).iterator();

        while(var5.hasNext()) {
            String arquivo = (String)var5.next();
            importacaoManualModel.processaPlanilha(arquivo);
        }

    }

    public void gerarDadosContabeis(ContextoAcao contextoAcao) throws Exception {
        ImportacaoManualModel importacaoManualModel = new ImportacaoManualModel();
        Registro linha = contextoAcao.getLinhas()[0];
        BigDecimal nuImpMan = new BigDecimal(linha.getCampo("NUIMPMAN").toString());
        importacaoManualModel.setNumeroUnicoImportacaoManual(nuImpMan);
        importacaoManualModel.setCodigoUsuario(contextoAcao.getUsuarioLogado());
        importacaoManualModel.gerarLancamentosContabeis();
    }

    public void gerarDadosGerenciais(ContextoAcao contextoAcao) throws Exception {
        ImportacaoManualModel importacaoManualModel = new ImportacaoManualModel();
        Registro linha = contextoAcao.getLinhas()[0];
        BigDecimal nuImpMan = new BigDecimal(linha.getCampo("NUIMPMAN").toString());
        importacaoManualModel.setNumeroUnicoImportacaoManual(nuImpMan);
        importacaoManualModel.setCodigoUsuario(contextoAcao.getUsuarioLogado());
        importacaoManualModel.gerarLancamentosGerenciais();
    }

    public void beforeDeleteCabecalho(PersistenceEvent persistenceEvent) throws Exception {
        ImportacaoManualModel importacaoManualModel = new ImportacaoManualModel();
        BigDecimal numeroUnicoImportacaomanual = ((DynamicVO)persistenceEvent.getVo()).asBigDecimal("NUIMPMAN");
        importacaoManualModel.setNumeroUnicoImportacaoManual(numeroUnicoImportacaomanual);
        importacaoManualModel.excluirMovimentacaoGerencialDaImportacaoManual();
    }
}
