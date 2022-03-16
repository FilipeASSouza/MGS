package br.com.mgs.EPI;

import br.com.mgs.utils.BuscaDadosFuncionario;
import br.com.mgs.utils.ErroUtils;
import br.com.mgs.utils.NativeSqlDecorator;
import br.com.mgs.utils.LerArquivoDeDadosDecorator;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class ImportarPedidoRequisicaoEPI {
    private BigDecimal numeroUnico;
    private Timestamp dataTipoNegociacao;
    private Timestamp dataTipoOperacao;
    private BigDecimal codigoUsuario;
    private String mensagem;
    private JapeWrapper usuariosDAO = JapeFactory.dao("Usuario");
    private JapeWrapper dao = JapeFactory.dao("AD_IMPPEDREQEPI");
    private JapeWrapper cabecalhoDAO = JapeFactory.dao("CabecalhoNota");//TGFCAB
    private JapeWrapper itemDAO = JapeFactory.dao("ItemNota");//TGFITE
    private JapeWrapper produtoDAO = JapeFactory.dao("Produto");//TGFPRO
    private JapeWrapper contatoDAO = JapeFactory.dao("Contato");//TGFCTT
    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();
    private NativeSqlDecorator tipoNegociacaoDecorator;
    private NativeSqlDecorator consultandoDhTipoOperacaoSQL;
    private DynamicVO vo;
    private DynamicVO usuariosVO;

    public void setNumeroUnico(BigDecimal numeroUnico){
        this.numeroUnico = numeroUnico;
    }

    public void importar() throws Exception {

        usuariosVO = usuariosDAO.findByPK(AuthenticationInfo.getCurrent().getUserID());

        try{
            vo = dao.findByPK(numeroUnico);

            if("P".equals(vo.asString("STATUS"))){
                ErroUtils.disparaErro("Arquivo ja processado!");
            }

            for( String arquivo : getListaDeArquivos("AD_IMPPEDREQEPI", numeroUnico )){
                processaPlanilha(arquivo);
            }

            FluidUpdateVO fuvo = dao.prepareToUpdate(vo);
            fuvo.set("STATUS", "P");
            fuvo.set("LOG", mensagem == null ? "OK - Pedidos Gerados!" : mensagem);
            fuvo.set("CODUSU", usuariosVO.asBigDecimalOrZero("CODUSU"));
            fuvo.set("NOMEUSU", usuariosVO.asString("AD_APRESENTACAO"));
            fuvo.set("DHINCLUSAO", TimeUtils.getNow());
            fuvo.update();
        }catch( Exception e ){
            FluidUpdateVO fuvo = dao.prepareToUpdate(vo);
            fuvo.set("STATUS", "E");
            fuvo.set("LOG", "Erro ao processar o arquivo: " + e);
            fuvo.set("CODUSU", usuariosVO.asBigDecimalOrZero("CODUSU"));
            fuvo.set("NOMEUSU", usuariosVO.asString("AD_APRESENTACAO"));
            fuvo.set("DHINCLUSAO", TimeUtils.getNow());
            fuvo.update();
        }finally {
            if(tipoNegociacaoDecorator != null){
                tipoNegociacaoDecorator.close();
            }

            if( consultandoDhTipoOperacaoSQL != null ){
                consultandoDhTipoOperacaoSQL.close();
            }
        }
    }

    public void processaPlanilha( String arquivo ) throws Exception{

        try{

            LerArquivoDeDadosDecorator planilha = new LerArquivoDeDadosDecorator(arquivo,"xlsx");

            planilha.setColuna("CODTIPOPER", 0);
            planilha.setColuna("CODPARC", 1);
            planilha.setColuna("TROCA", 2);
            planilha.setColuna("MATRICULA", 3);
            planilha.setColuna("NOME", 4);
            planilha.setColuna("CODLOT", 5);
            planilha.setColuna("CODCENCUS", 6);
            planilha.setColuna("CODPROJ", 7);
            planilha.setColuna("NUMCONTRATO", 8);
            planilha.setColuna("CODNAT", 9);
            planilha.setColuna("CODPROD", 10);
            planilha.setColuna("DESCRPROD", 11);
            planilha.setColuna("CODVOL", 12);
            planilha.setColuna("QTDNEG", 13);
            planilha.setColuna("CONTROLE", 14);
            planilha.setColuna("VLRUNIT", 15);
            planilha.setColuna("CODLOCALORIG", 16);

            //Pegando a data do tipo de negociacao
            tipoNegociacaoDecorator = new NativeSqlDecorator("SELECT MAX(DHALTER) AS DHALTER FROM TGFTPV WHERE CODTIPVENDA = :CODTIPVENDA", jdbcWrapper );
            tipoNegociacaoDecorator.setParametro("CODTIPVENDA", BigDecimal.valueOf(52L) );

            if( tipoNegociacaoDecorator.proximo() ){
                dataTipoNegociacao = tipoNegociacaoDecorator.getValorTimestamp("DHALTER");
            }

            //Pegando a data do tipo de operacao
            consultandoDhTipoOperacaoSQL = new NativeSqlDecorator("SELECT MAX(DHALTER) DHALTER FROM TGFTOP WHERE CODTIPOPER = :CODTIPOPER", jdbcWrapper );
            consultandoDhTipoOperacaoSQL.setParametro("CODTIPOPER", BigDecimal.valueOf(303L) );
            if(consultandoDhTipoOperacaoSQL.proximo()){
                dataTipoOperacao = consultandoDhTipoOperacaoSQL.getValorTimestamp("DHALTER");
            }

            while(planilha.proximo()){

                DynamicVO contatoVO = contatoDAO.findOne("CODPARC = ? AND AD_CODLOT =?"
                        , new Object[]{planilha.getValorBigDecimal("CODPARC")
                                , planilha.getValorBigDecimal("CODLOT")});
                BigDecimal contato = contatoVO.asBigDecimalOrZero("CODCONTATO");
                BigDecimal valorTotal = planilha.getValorBigDecimal2("QTDNEG").multiply(planilha.getValorBigDecimal2("VLRUNIT"));

                FluidCreateVO cabecalhoNotaFCVO = cabecalhoDAO.create();
                cabecalhoNotaFCVO.set("NUMNOTA", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("NUMCONTRATO", planilha.getValorBigDecimal("NUMCONTRATO"));
                cabecalhoNotaFCVO.set("TIPMOV", "J" );
                cabecalhoNotaFCVO.set("AD_TROCA", "NAO".equals(planilha.getValorString("TROCA")) ? String.valueOf("1") : String.valueOf("2") );
                cabecalhoNotaFCVO.set("CODEMP", BigDecimal.ONE );
                cabecalhoNotaFCVO.set("CODPARC", planilha.getValorBigDecimal("CODPARC"));
                cabecalhoNotaFCVO.set("CODCENCUS", planilha.getValorBigDecimal("CODCENCUS") );
                cabecalhoNotaFCVO.set("CODNAT", planilha.getValorBigDecimal("CODNAT") );
                cabecalhoNotaFCVO.set("SERIENOTA", null );
                cabecalhoNotaFCVO.set("AD_MATRICULA", planilha.getValorBigDecimal("MATRICULA").toString() );
                cabecalhoNotaFCVO.set("AD_MATRICULANOME", planilha.getValorString("NOME") );
                cabecalhoNotaFCVO.set("DTENTSAI", TimeUtils.getNow() );
                cabecalhoNotaFCVO.set("DTNEG", TimeUtils.getNow() );
                cabecalhoNotaFCVO.set("DTFATUR", null );
                cabecalhoNotaFCVO.set("DTMOV", TimeUtils.getNow() );
                cabecalhoNotaFCVO.set("CODTIPOPER", BigDecimal.valueOf(303L) );
                cabecalhoNotaFCVO.set("CODEMPNEGOC", BigDecimal.ONE);
                cabecalhoNotaFCVO.set("CIF_FOB", String.valueOf("F") );
                cabecalhoNotaFCVO.set("DHTIPOPER", this.dataTipoOperacao );
                cabecalhoNotaFCVO.set("CODPROJ", planilha.getValorBigDecimal("CODPROJ"));
                cabecalhoNotaFCVO.set("OBSERVACAO", null );
                cabecalhoNotaFCVO.set("CODUSU", this.codigoUsuario );
                cabecalhoNotaFCVO.set("CODUSUINC", this.codigoUsuario );
                cabecalhoNotaFCVO.set("VLRNOTA", valorTotal );
                cabecalhoNotaFCVO.set("STATUSNOTA", "A" );
                cabecalhoNotaFCVO.set("PENDENTE", String.valueOf("S"));
                cabecalhoNotaFCVO.set("CODTIPVENDA", BigDecimal.valueOf(52L) );
                cabecalhoNotaFCVO.set("DHTIPVENDA", this.dataTipoNegociacao );
                cabecalhoNotaFCVO.set("AD_CODLOT", planilha.getValorBigDecimal("CODLOT"));
                cabecalhoNotaFCVO.set("CHAVENFE", null );
                cabecalhoNotaFCVO.set("CODCONTATO", contato );
                cabecalhoNotaFCVO.set("AD_DTVENC", null );
                cabecalhoNotaFCVO.set("QTDVOL", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("PESO", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("TOTALCUSTOPROD", BigDecimal.valueOf(2.85342) ); //Custo Reposicao
                cabecalhoNotaFCVO.set("TOTALCUSTOSERV", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("PESOBRUTO", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("BASEIRF", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("ALIQIRF", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("VLRSTEXTRANOTATOT", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("CODCIDORIGEM", null );
                cabecalhoNotaFCVO.set("CODCIDDESTINO", null );
                cabecalhoNotaFCVO.set("CODCIDENTREGA", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("CODUFORIGEM", null );
                cabecalhoNotaFCVO.set("CODUFDESTINO", null );
                cabecalhoNotaFCVO.set("CODUFENTREGA", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("CLASSIFICMS", null );
                cabecalhoNotaFCVO.set("VLRREPREDTOTSEMDESC", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("VLRFETHAB", BigDecimal.ZERO );
                cabecalhoNotaFCVO.set("TIPFRETE", "N");
                cabecalhoNotaFCVO.set("APROVADO", "N");
                cabecalhoNotaFCVO.set("CODEMPFUNC", BigDecimal.ONE );
                DynamicVO notaDestino = cabecalhoNotaFCVO.save();

                FluidCreateVO itemFCVO = itemDAO.create();
                itemFCVO.set("NUNOTA", notaDestino.asBigDecimalOrZero("NUNOTA") );
                itemFCVO.set("CODPROD", planilha.getValorBigDecimal("CODPROD") );
                itemFCVO.set("QTDNEG", planilha.getValorBigDecimal("QTDNEG"));
                DynamicVO produtoVO = produtoDAO.findByPK(planilha.getValorBigDecimal("CODPROD"));
                itemFCVO.set("CODVOL", produtoVO.asString("CODVOL"));
                itemFCVO.set("ATUALESTOQUE", BigDecimal.ZERO);
                itemFCVO.set("STATUSNOTA", "A" );
                itemFCVO.set("CONTROLE", planilha.getValorString("CONTROLE"));
                itemFCVO.set("USOPROD", produtoVO.asString("USOPROD") );
                itemFCVO.set("VLRUNIT", planilha.getValorBigDecimal2("VLRUNIT"));
                itemFCVO.set("VLRTOT", valorTotal );
                itemFCVO.set("RESERVA", "N");
                itemFCVO.set("PENDENTE", "N");
                itemFCVO.set("CODLOCALORIG", planilha.getValorBigDecimal("CODLOCALORIG"));
                itemFCVO.set("VLRCUS", planilha.getValorBigDecimal2("VLRUNIT"));
                itemFCVO.set("SOLCOMPRA", String.valueOf("N"));
                itemFCVO.set("ATUALESTTERC", String.valueOf("N"));
                itemFCVO.set("TERCEIROS", String.valueOf("N"));
                itemFCVO.set("PRECOBASE", planilha.getValorBigDecimal2("VLRUNIT"));
                itemFCVO.set("CUSTO", BigDecimal.valueOf(2.85342)); //Custo Reposicao
                itemFCVO.save();
            }
        }catch(Exception e){
            System.out.println(e);
            mensagem = e.toString();
            e.printStackTrace();
        }
    }

    private Collection<String> getListaDeArquivos(String instancia, BigDecimal numeroUnico) {

        //pega a lista de arquivos anexados
        Collection<DynamicVO> listaAnexoSistema = new ArrayList();
        Collection<String> listaArquivos = new ArrayList();
        try {
            listaAnexoSistema = JapeFactory.dao("AnexoSistema")
                    .find("NOMEINSTANCIA = ? AND PKREGISTRO = ?", instancia, numeroUnico.toString() + "_" + instancia);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        String diretorioBase = "";
        try {
            diretorioBase = JapeFactory.dao("ParametroSistema").findOne("CHAVE = 'FREPBASEFOLDER'").asString("TEXTO");
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        String diretorioArquivo = diretorioBase + "/Sistema/Anexos/" + instancia + "/";


        for (DynamicVO anexoSitema : listaAnexoSistema) {

            ArrayList listaLinhasArquivo = new ArrayList();

            String arquivo = diretorioArquivo + anexoSitema.asString("CHAVEARQUIVO");
            listaArquivos.add(arquivo);

        }

        return listaArquivos;
    }
}
