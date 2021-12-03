package br.com.mgs.diariasDeViagem.acao;

import br.com.mgs.utils.BuscaDadosWebService;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ReintegrarDiariaAutorizadaAcao implements AcaoRotinaJava {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();


    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        /*    Parametros Utilizadas para a Chamada      */


        String ambienteServidor = "http://192.168.7.161:8085/api/";
        String operacao = "sincronizarDiariaAut";
        String url = ambienteServidor + operacao;
        String numeroDiaria = contextoAcao.getParam("NUDIARIA").toString();
        String param = "{\"" + operacao + "\":{\"cod_autorizacao_viagem\":\"" + numeroDiaria + "\"}}";

        try{

            BuscaDadosWebService buscaDadosWebService = new BuscaDadosWebService();
            String resposta = buscaDadosWebService.buscar(url,param);


        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
