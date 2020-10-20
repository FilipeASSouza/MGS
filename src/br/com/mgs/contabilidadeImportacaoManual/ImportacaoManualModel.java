package br.com.mgs.contabilidadeImportacaoManual;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.bh.CentralNotasUtils;
import br.com.sankhya.bh.TimeUtilsKt;
import br.com.sankhya.bh.dao.DynamicVOKt;
import br.com.sankhya.bh.dao.EntityFacadeW;
import br.com.sankhya.bh.dao.WrapperVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


public class ImportacaoManualModel {
    private Collection<DadosPlanilhaPOJO> dadoPlanilha = new ArrayList();
    private BigDecimal numeroUnicoImportacaoManual;
    private EntityFacadeW facadeW = new EntityFacadeW();
    private BigDecimal codigoUsuario;

    public ImportacaoManualModel() {
    }

    public Collection<String> getListaDeArquivos(String instancia, BigDecimal numeroUnico) {
        Collection<DynamicVO> listaAnexoSistema = new ArrayList();
        ArrayList listaArquivos = new ArrayList();

        try {
            listaAnexoSistema = JapeFactory.dao("AnexoSistema").find("NOMEINSTANCIA = ? AND PKREGISTRO = ?", new Object[]{instancia, numeroUnico.toString() + "_" + instancia});
        } catch (Exception var12) {
            System.out.println(var12);
            var12.printStackTrace();
        }

        String diretorioBase = "";

        try {
            diretorioBase = JapeFactory.dao("ParametroSistema").findOne("CHAVE = 'FREPBASEFOLDER'").asString("TEXTO");
        } catch (Exception var11) {
            System.out.println(var11);
            var11.printStackTrace();
        }

        String diretorioArquivo = diretorioBase + "/Sistema/Anexos/" + instancia + "/";
        Iterator var7 = ((Collection)listaAnexoSistema).iterator();

        while(var7.hasNext()) {
            DynamicVO anexoSitema = (DynamicVO)var7.next();
            new ArrayList();
            String arquivo = diretorioArquivo + anexoSitema.asString("CHAVEARQUIVO");
            listaArquivos.add(arquivo);
        }

        return listaArquivos;
    }

    public void processaPlanilha(String arquivo) throws Exception {
        byte numlinha = 1;

        try {
            Workbook workbook = Workbook.getWorkbook(new File(arquivo));
            Sheet sheet = workbook.getSheet(0);
            int linhas = sheet.getRows();

            for(numlinha = 1; numlinha < linhas; ++numlinha) {
                DadosPlanilhaPOJO dadosPlanilhaPOJO = new DadosPlanilhaPOJO();
                dadosPlanilhaPOJO.setDocumento(new BigDecimal(sheet.getCell(0, numlinha).getContents()));
                dadosPlanilhaPOJO.setNumeroDoLote(new BigDecimal(sheet.getCell(1, numlinha).getContents()));
                dadosPlanilhaPOJO.setReferencia(new Timestamp((new SimpleDateFormat("ddMMyyyy")).parse(sheet.getCell(2, numlinha).getContents()).getTime()));
                dadosPlanilhaPOJO.setCodigoEmpresa(new BigDecimal(sheet.getCell(3, numlinha).getContents()));
                dadosPlanilhaPOJO.setCodigoCentroDeCusto(new BigDecimal(sheet.getCell(4, numlinha).getContents()));
                dadosPlanilhaPOJO.setCodigoNatureza(new BigDecimal(sheet.getCell(5, numlinha).getContents()));
                dadosPlanilhaPOJO.setCodigoSite(new BigDecimal(sheet.getCell(6, numlinha).getContents()));
                dadosPlanilhaPOJO.setCodigoContaContabilReduzida(new BigDecimal(sheet.getCell(7, numlinha).getContents()));
                dadosPlanilhaPOJO.setCodigoHistoricoPadrao(new BigDecimal(sheet.getCell(8, numlinha).getContents()));
                dadosPlanilhaPOJO.setComplementoHistorico(sheet.getCell(9, numlinha).getContents());
                dadosPlanilhaPOJO.setTipoLancamento(sheet.getCell(10, numlinha).getContents().substring(0, 1));
                dadosPlanilhaPOJO.setValor(new BigDecimal(sheet.getCell(11, numlinha).getContents().replace(".", "").replace(",", ".")));
                dadosPlanilhaPOJO.setCodigoProjeto(new BigDecimal(sheet.getCell(12, numlinha).getContents()));
                this.dadoPlanilha.add(dadosPlanilhaPOJO);
                System.out.println(dadosPlanilhaPOJO.toString());
            }

            workbook.close();
        } catch (Exception var7) {
            ErroUtils.disparaErro("Erro na linha: " + Integer.toString(numlinha + 1) + " Erro :" + var7.toString());
        }

        JapeFactory.dao("AD_TCBIMPMANLOG").create().set("NUIMPMAN", this.numeroUnicoImportacaoManual).set("DHREGISTRO", TimeUtils.getNow()).set("LOG", "Planilha Carregada").save();
        JapeWrapper ad_tcbimpmaniteDAO = JapeFactory.dao("AD_TCBIMPMANITE");
        Iterator var10 = this.dadoPlanilha.iterator();

        while(var10.hasNext()) {
            DadosPlanilhaPOJO dados = (DadosPlanilhaPOJO)var10.next();
            ad_tcbimpmaniteDAO.create().set("NUIMPMAN", this.numeroUnicoImportacaoManual).set("DOCUMENTO", dados.getDocumento()).set("NUMLOTE", dados.getNumeroDoLote()).set("DTREF", dados.getReferencia()).set("CODEMP", dados.getCodigoEmpresa()).set("CODCENCUS", dados.getCodigoCentroDeCusto()).set("CODNAT", dados.getCodigoNatureza()).set("CODSITE", dados.getCodigoSite()).set("CODCTACTB", dados.getCodigoContaContabilReduzida()).set("CODHISTCTB", dados.getCodigoHistoricoPadrao()).set("COMPLHIST", dados.getComplementoHistorico()).set("TIPLANC", dados.getTipoLancamento()).set("VALOR", dados.getValor()).set("CODPROJ", dados.getCodigoProjeto()).save();
        }

        JapeFactory.dao("AD_TCBIMPMANLOG").create().set("NUIMPMAN", this.numeroUnicoImportacaoManual).set("DHREGISTRO", TimeUtils.getNow()).set("LOG", "Planilha salva na tela").save();
    }

    public void gerarLancamentosContabeis() throws Exception {
        Integer sequencia = 0;

        try {
            JapeWrapper ad_tcbimpmaniteDAO = JapeFactory.dao("AD_TCBIMPMANITE");
            JapeWrapper lancamentoDAO = JapeFactory.dao("Lancamento");
            FinderWrapper finderWrapper = new FinderWrapper("AD_TCBIMPMANITE", "NUIMPMAN = ?", this.numeroUnicoImportacaoManual);
            finderWrapper.setMaxResults(-1);
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            Collection ad_tcbimpmaniteVOS = dwfFacade.findByDynamicFinderAsVO(finderWrapper);
            int i = 1;
            Iterator var8 = ad_tcbimpmaniteVOS.iterator();

            while(var8.hasNext()) {
                Object element = var8.next();
                DynamicVO ad_tcbimpmaniteVO = (DynamicVO)element;
                this.criarLote(ad_tcbimpmaniteVO.asTimestamp("DTREF"), ad_tcbimpmaniteVO.asBigDecimal("CODEMP"), ad_tcbimpmaniteVO.asBigDecimal("NUMLOTE"));
                sequencia = ad_tcbimpmaniteVO.asInt("SEQUENCIA");
                DynamicVO saveVO = lancamentoDAO.create().set("CODCTACTB", ad_tcbimpmaniteVO.asBigDecimal("CODCTACTB")).set("NUMDOC", ad_tcbimpmaniteVO.asBigDecimal("DOCUMENTO")).set("VLRLANC", ad_tcbimpmaniteVO.asBigDecimal("VALOR")).set("TIPLANC", ad_tcbimpmaniteVO.asString("TIPLANC").equals("D") ? "D" : "R").set("LIBERADO", "S").set("CODHISTCTB", ad_tcbimpmaniteVO.asBigDecimal("CODHISTCTB")).set("REFERENCIA", TimeUtilsKt.getMonthStart(ad_tcbimpmaniteVO.asTimestamp("DTREF"))).set("CODEMP", ad_tcbimpmaniteVO.asBigDecimal("CODEMP")).set("CODUSU", this.codigoUsuario).set("DTMOV", ad_tcbimpmaniteVO.asTimestamp("DTREF")).set("NUMLOTE", ad_tcbimpmaniteVO.asBigDecimal("NUMLOTE")).set("NUMLANC", ad_tcbimpmaniteVO.asBigDecimal("SEQUENCIA")).set("COMPLHIST", ad_tcbimpmaniteVO.asString("COMPLHIST")).set("CODCENCUS", ad_tcbimpmaniteVO.asBigDecimal("CODCENCUS")).set("SEQUENCIA", new BigDecimal(i++)).set("CODPROJ", ad_tcbimpmaniteVO.asBigDecimal("CODPROJ")).save();
                ad_tcbimpmaniteDAO.prepareToUpdate(ad_tcbimpmaniteVO).set("NUMLANC", saveVO.asBigDecimal("NUMLANC")).update();
            }

            JapeFactory.dao("AD_TCBIMPMANLOG").create().set("NUIMPMAN", this.numeroUnicoImportacaoManual).set("DHREGISTRO", TimeUtils.getNow()).set("LOG", "Dados Contabeis lancandos").save();
        } catch (Exception var12) {
            ErroUtils.disparaErro("Registro de Sequencia : " + sequencia + " Erro :" + var12.toString());
        }

    }

    private void criarLote(Timestamp dtRef, BigDecimal codEmp, BigDecimal numLote) throws Exception {
        Collection<WrapperVO> findByFinder = this.facadeW.findByFinder("MestreLote", "CODEMP = ? AND REFERENCIA = ? AND NUMLOTE =?", new Object[]{codEmp, TimeUtilsKt.getMonthStart(dtRef), numLote});
        if (findByFinder.isEmpty()) {
            DynamicVO novodebitoLote = this.facadeW.newVO("MestreLote");
            novodebitoLote.setProperty("CODEMP", codEmp);
            novodebitoLote.setProperty("REFERENCIA", TimeUtilsKt.getMonthStart(dtRef));
            novodebitoLote.setProperty("NUMLOTE", numLote);
            novodebitoLote.setProperty("DTMOV", dtRef);
            novodebitoLote.setProperty("SITUACAO", "A");
            novodebitoLote.setProperty("ULTLANC", BigDecimal.ZERO);
            this.facadeW.createEntity(novodebitoLote);
        }

    }

    public void gerarLancamentosGerenciais() throws Exception {
        EntityFacade facade = EntityFacadeFactory.getCoreFacade();
        JdbcWrapper jdbcWrapper = facade.getJdbcWrapper();
        NativeSql sqlNota = new NativeSql(jdbcWrapper);
        Connection connection = jdbcWrapper.getConnection();
        ResultSet result = null;
        ResultSet resultRateio = null;
        NativeSql sqlRateio = null;

        try {
            JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
            JapeWrapper itensnotaDAO = JapeFactory.dao("ItemNota");
            JapeWrapper produtoDAO = JapeFactory.dao("Produto");
            JapeWrapper rateioRecDesp = JapeFactory.dao("RateioRecDesp");
            this.excluirMovimentacaoGerencialDaImportacaoManual();
            sqlNota.appendSql("SELECT NUIMPMAN, TRUNC(DTREF,'MONTH'), CODEMP, CODCTACTB, TIPLANC,TRUNC(DTREF,'MONTH') AS DATA, SUM(VALOR) AS VALORTOTAL \nFROM AD_TCBIMPMANITE\nWHERE NUIMPMAN = :NUIMPMAN\nGROUP BY NUIMPMAN, TRUNC(DTREF,'MONTH'), CODEMP, TIPLANC, CODCTACTB");
            sqlNota.setNamedParameter("NUIMPMAN", this.numeroUnicoImportacaoManual);
            result = sqlNota.executeQuery();
            BigDecimal numeroUnicoNota = BigDecimal.ONE;

            while(result.next()) {
                String tipoLancamento = result.getString("TIPLANC");
                BigDecimal valorTotal = result.getBigDecimal("VALORTOTAL");
                Timestamp data = result.getTimestamp("DATA");
                BigDecimal numeroUnicoModelo = null;
                if ("C".equals(tipoLancamento)) {
                    numeroUnicoModelo = new BigDecimal(50509);
                } else if ("D".equals(tipoLancamento)) {
                    numeroUnicoModelo = new BigDecimal(50510);
                }

                DynamicVO modeloNotaVO = cabecalhoNotaDAO.findByPK(new Object[]{numeroUnicoModelo});
                Map<String, Object> campos = new HashMap();
                campos.put("TIPMOV", modeloNotaVO.asString("TipoOperacao.TIPMOV"));
                campos.put("NUMCONTRATO", BigDecimal.ZERO);
                campos.put("DTNEG", data);
                campos.put("DTENTSAI", data);
                campos.put("CODPROJ", BigDecimal.ZERO);
                campos.put("RATEADO", "S");
                DynamicVO notaDestino = DynamicVOKt.duplicaRegistro(modeloNotaVO, campos);
                numeroUnicoNota = notaDestino.asBigDecimal("NUNOTA");
                DynamicVO produtoVO = produtoDAO.findByPK(new Object[]{BigDecimal.ONE});
                FluidCreateVO itemNota = itensnotaDAO.create();
                itemNota.set("NUNOTA", notaDestino.asBigDecimal("NUNOTA"));
                itemNota.set("CODPROD", BigDecimal.ONE);
                itemNota.set("QTDNEG", BigDecimal.ONE);
                itemNota.set("ATUALESTOQUE", BigDecimal.ONE);
                itemNota.set("RESERVA", "S");
                itemNota.set("STATUSNOTA", "A");
                itemNota.set("USOPROD", produtoVO.asString("USOPROD"));
                itemNota.set("CONTROLE", " ");
                itemNota.set("CODVOL", produtoVO.asString("CODVOL"));
                itemNota.set("VLRUNIT", valorTotal);
                itemNota.set("VLRTOT", valorTotal);
                itemNota.set("ATUALESTOQUE", BigDecimal.ZERO);
                itemNota.set("RESERVA", "N");
                itemNota.save();
                sqlRateio = new NativeSql(jdbcWrapper);
                sqlRateio.appendSql("SELECT NUIMPMAN,TRUNC(DTREF,'MONTH') DTREF, CODEMP, CODCENCUS, CODNAT, CODSITE,CODPROJ, CODCTACTB, TIPLANC, SUM(VALOR) AS VALORTOTAL,  SUM(SUM(VALOR)) OVER (PARTITION BY NUIMPMAN, TIPLANC) AS VALOR, SUM(VALOR)/SUM(SUM(VALOR)) OVER (PARTITION BY NUIMPMAN, TIPLANC) * 100 AS PERC  FROM AD_TCBIMPMANITE WHERE NUIMPMAN = :NUIMPMAN AND TIPLANC = :TIPLANC AND CODCTACTB = :CODCTACTB GROUP BY NUIMPMAN, TRUNC(DTREF,'MONTH'), CODEMP, CODCENCUS, CODNAT, CODSITE, CODCTACTB, TIPLANC, CODPROJ ");
                sqlRateio.setNamedParameter("NUIMPMAN", this.numeroUnicoImportacaoManual);
                sqlRateio.setNamedParameter("TIPLANC", tipoLancamento);
                sqlRateio.setNamedParameter("CODCTACTB", result.getBigDecimal("CODCTACTB"));
                resultRateio = sqlRateio.executeQuery();

                while(resultRateio.next()) {
                    rateioRecDesp.create().set("ORIGEM", "E").set("NUFIN", notaDestino.asBigDecimal("NUNOTA")).set("CODNAT", resultRateio.getBigDecimal("CODNAT")).set("CODCENCUS", resultRateio.getBigDecimal("CODCENCUS")).set("CODPROJ", resultRateio.getBigDecimal("CODPROJ")).set("CODSITE", resultRateio.getBigDecimal("CODSITE")).set("CODPARC", BigDecimal.ZERO).set("PERCRATEIO", resultRateio.getBigDecimal("PERC")).set("CODCTACTB", resultRateio.getBigDecimal("CODCTACTB")).save();
                }

                NativeSql sqlUpdateIens = new NativeSql(jdbcWrapper);
                sqlUpdateIens.appendSql("UPDATE AD_TCBIMPMANITE SET NUNOTA = :NUNOTA ");
                sqlUpdateIens.appendSql("WHERE NUIMPMAN = :NUIMPMAN AND CODCTACTB = :CODCTACTB");
                sqlUpdateIens.setNamedParameter("NUNOTA", numeroUnicoNota);
                sqlUpdateIens.setNamedParameter("NUIMPMAN", this.numeroUnicoImportacaoManual);
                sqlUpdateIens.setNamedParameter("CODCTACTB", result.getBigDecimal("CODCTACTB"));
                sqlUpdateIens.executeUpdate();
                if (!"L".equals(notaDestino.asString("STATUSNOTA"))) {
                    CentralNotasUtils.confirmarNota(numeroUnicoNota);
                }
            }
        } finally {
            if (jdbcWrapper != null) {
                JdbcWrapper.closeSession(jdbcWrapper);
            }

            if (sqlNota != null) {
                NativeSql.releaseResources(sqlNota);
            }

            if (sqlRateio != null) {
                NativeSql.releaseResources(sqlRateio);
            }

            if (result != null) {
                result.close();
            }

            if (resultRateio != null) {
                resultRateio.close();
            }

        }

    }

    public void excluirMovimentacaoGerencialDaImportacaoManual() throws Exception {
        EntityFacade facade = EntityFacadeFactory.getCoreFacade();
        JdbcWrapper jdbcWrapper = facade.getJdbcWrapper();
        NativeSql sqlNotasAExcluir = new NativeSql(jdbcWrapper);
        JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
        sqlNotasAExcluir.appendSql("SELECT NUNOTA \nFROM AD_TCBIMPMANITE\nWHERE NUIMPMAN = :NUIMPMAN \nAND NUNOTA IS NOT NULL\nGROUP BY NUNOTA");
        sqlNotasAExcluir.setNamedParameter("NUIMPMAN", this.numeroUnicoImportacaoManual);
        ResultSet resultSetNotasAExcluir = sqlNotasAExcluir.executeQuery();

        while(resultSetNotasAExcluir.next()) {
            cabecalhoNotaDAO.deleteByCriteria("NUNOTA = ?", new Object[]{resultSetNotasAExcluir.getBigDecimal("NUNOTA")});
        }

    }

    public void gerarLancamentosGerenciaisPorImportacao(BigDecimal numeroUnicoImportacaoManual) {
    }

    public void gerarLancamentosGerenciaisPorContaContabil(BigDecimal contaCotnabil) {
    }

    public BigDecimal getNumeroUnicoImportacaoManual() {
        return this.numeroUnicoImportacaoManual;
    }

    public void setNumeroUnicoImportacaoManual(BigDecimal numeroUnicoImportacaoManual) {
        this.numeroUnicoImportacaoManual = numeroUnicoImportacaoManual;
    }

    public BigDecimal getCodigoUsuario() {
        return this.codigoUsuario;
    }

    public void setCodigoUsuario(BigDecimal codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }
}
