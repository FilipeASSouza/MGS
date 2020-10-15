import br.com.sankhya.bh.contabilidadeImportacaoManual.ImportacaoManualController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class ImportarPlanilhaAcao implements AcaoRotinaJava {

    public ImportarPlanilhaAcao(){}

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        (new ImportacaoManualController()).importaPlanilha(contextoAcao);
    }
}
