import br.com.sankhya.bh.contabilidadeImportacaoManual.ImportacaoManualController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class GerarDadosContabeisAcao implements AcaoRotinaJava {

    public GerarDadosContabeisAcao() {
    }

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        (new ImportacaoManualController()).gerarDadosContabeis(contextoAcao);
    }
}
