import br.com.sankhya.bh.contabilidadeImportacaoManual.ImportacaoManualController;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class GerarDadosGerenciaisAcao implements AcaoRotinaJava {

    public GerarDadosGerenciaisAcao(){}

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        (new ImportacaoManualController()).gerarDadosGerenciais(contextoAcao);
    }
}
