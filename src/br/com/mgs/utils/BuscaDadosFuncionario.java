package br.com.mgs.utils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class BuscaDadosFuncionario {

    public BuscaDadosFuncionario() {
    }

    public void executar(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();

        MGECoreParameter.getParameterAsString("PARAMBARRAMENTO");
        String matricula = vo.asString("AD_MATRICULA");
        String ambienteServidor = MGECoreParameter.getParameterAsString("PARAMBARRAMENTO");
        String operacao = "PesquisarMatricula";
        String url = ambienteServidor + operacao;
        String param = "{\"" + operacao + "\":{\"MATRICULA\":\"" + matricula + "\"}}";

        try {
            BuscaDadosWebService buscaDadosWebService = new BuscaDadosWebService();
            String resposta = buscaDadosWebService.buscar(url, param);
            PesquisarMatricula.DadosFuncionario funcionario = (new PesquisarMatricula()).getFuncionario(resposta);
            String matriculaBarramento = (funcionario.ListMatricula.RowMatricula.get(0)).MATRICULA;
            String nome = (funcionario.ListMatricula.RowMatricula.get(0)).NOME;
            String email = (funcionario.ListMatricula.RowMatricula.get(0)).EMAIL;
            String areaRisco = (funcionario.ListMatricula.RowMatricula.get(0)).AREA_RISCO;
            String situacao = (funcionario.ListMatricula.RowMatricula.get(0)).SITUACAO;
            BigDecimal lotacao = (funcionario.ListMatricula.RowMatricula.get(0)).LOTACAO;
            BigDecimal sapato = (funcionario.ListMatricula.RowMatricula.get(0)).SAPATO;
            BigDecimal calca = (funcionario.ListMatricula.RowMatricula.get(0)).CALCA;
            BigDecimal camisa = (funcionario.ListMatricula.RowMatricula.get(0)).CAMISA;
            BigDecimal paleto = (funcionario.ListMatricula.RowMatricula.get(0)).PALETO;
            String dataInicioVigencia = (funcionario.ListMatricula.RowMatricula.get(0)).DATA_INI_VIGENCIA;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatando = new SimpleDateFormat("yyyy-MM-dd");
            Date dt = sdf.parse(dataInicioVigencia);

            Timestamp data = Timestamp.valueOf(formatando.format(dt)+ " 00:00:00");
            vo.setProperty("AD_AREA_RISCO", areaRisco);
            vo.setProperty("AD_DATA_INI_VIGENCIA", data ); //formato correto yyyy-MM-dd HH:mm:ss
            vo.setProperty("AD_SITUACAO", situacao);
            vo.setProperty("AD_LOTACAOFUNC", lotacao);
            vo.setProperty("AD_CODLOT", lotacao);
            vo.setProperty("AD_SAPATO", sapato);
            vo.setProperty("AD_CALCA", calca);
            vo.setProperty("AD_CAMISA", camisa);
            vo.setProperty("AD_PALETO", paleto);

        } catch (Exception e) {
            System.out.println(e);
            throw new Exception(e);
        }

    }

    static class Request {
        boolean Success;
        Map<String, Object> Model;

        private Request() {
        }
    }
}
