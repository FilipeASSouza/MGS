package br.com.mgs.transferenciaBem;

import br.com.mgs.utils.ErroUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class TransfereBem implements EventoProgramavelJava {
    BigDecimal codlotant = null;
    String matriculaant = null;
    String matriculanomeant = null;
    BigDecimal codcencusant = null;
    BigDecimal codlocant = null;

    public TransfereBem() {
    }

    private void alterBem(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO transferenciaVO = (DynamicVO)persistenceEvent.getVo();
        JapeWrapper bemDAO = JapeFactory.dao("Imobilizado");
        JapeWrapper depDAO = JapeFactory.dao("Departamento");
        JapeWrapper locDAO = JapeFactory.dao("LocalAtual");
        JapeWrapper lotDAO = JapeFactory.dao("TGFLOT");
        BigDecimal matricula = transferenciaVO.asBigDecimal("MATRICULA");
        String matriculanome = transferenciaVO.asString("MATRICULANOME");
        BigDecimal codcencus = null != transferenciaVO.asBigDecimal("CODCENCUS") ? transferenciaVO.asBigDecimal("CODCENCUS") : BigDecimal.ZERO;
        BigDecimal codprod = transferenciaVO.asBigDecimal("CODPROD");
        BigDecimal codemp = transferenciaVO.asBigDecimal("CODEMP");
        BigDecimal codloc = transferenciaVO.asBigDecimal("CODLOC");
        String codbem = transferenciaVO.asString("CODBEM");
        DynamicVO coddepVO = locDAO.findOne("CODPROD = ? and CODBEM = ?", new Object[]{codprod, codbem});
        BigDecimal coddep = null;
        if (coddepVO != null) {
            coddep = coddepVO.asBigDecimal("CODDEPTO");
        } else {
            coddep = BigDecimal.ZERO;
            FluidCreateVO locVO = locDAO.create();
            locVO.set("CODPROD", codprod);
            locVO.set("CODBEM", codbem);
            locVO.set("CODEMP", codemp);
            locVO.set("CODDEPTO", BigDecimal.ZERO);
            locVO.set("SEQUENCIA", BigDecimal.ONE);
            locVO.set("CODUSU", BigDecimal.ZERO);
            locVO.set("DTENTRADA", TimeUtils.getNow());
            locVO.set("AD_CODCENCUS", codcencus);
            locVO.save();
            coddepVO = locDAO.findOne("CODPROD = ? and CODBEM = ?", new Object[]{codprod, codbem});
        }

        BigDecimal codlot = transferenciaVO.asBigDecimal("CODLOT");
        DynamicVO bemVO = bemDAO.findOne("CODPROD=? AND CODBEM=?", new Object[]{codprod, codbem});
        depDAO.findOne("CODCENCUS = ? AND ANALITICO = 'S'", new Object[]{codcencus});
        if (codcencus != BigDecimal.ZERO) {
            this.codlotant = null != bemVO.asBigDecimal("AD_CODLOT") ? bemVO.asBigDecimal("AD_CODLOT") : BigDecimal.ZERO;
            this.matriculaant = null != bemVO.asString("AD_MATRICULA") ? bemVO.asString("AD_MATRICULA") : "";
            this.matriculanomeant = null != bemVO.asString("AD_MATRICULANOME") ? bemVO.asString("AD_MATRICULANOME") : "";
            this.codlocant = null != bemVO.asBigDecimal("AD_CODLOC") ? bemVO.asBigDecimal("AD_CODLOC") : BigDecimal.ZERO;
            this.codcencusant = BigDecimalUtil.getValueOrZero(codcencus);
        } else {
            this.codcencusant = BigDecimal.ZERO;
        }

        String temCredPisCofinsDepr = this.getTemCredPisCofinsDepr(codlot);
        this.ativaDesativaCredPisCofinsDep(bemVO.asBigDecimal("CODPROD"), bemVO.asString("CODBEM"), temCredPisCofinsDepr);
        FluidUpdateVO bem = bemDAO.prepareToUpdate(bemVO);
        bem.set("AD_CODLOT", codlot);
        bem.set("AD_MATRICULA", matricula.toString());
        bem.set("AD_MATRICULANOME", matriculanome);
        bem.set("TEMCREDPISCOFINSDEPR", temCredPisCofinsDepr);
        bem.set("AD_CODLOC", codloc);
        bem.update();
        FluidUpdateVO dep = locDAO.prepareToUpdate(coddepVO);
        dep.set("AD_CODCENCUS", codcencus);
        dep.update();
        BigDecimal codNat = JapeFactory.dao("Produto").findByPK(new Object[]{codprod}).asBigDecimal("CODNAT");
        BigDecimal nuClassificassao = JapeFactory.dao("CentroResultado").findByPK(new Object[]{codcencus}).asBigDecimal("AD_NUCLASSIFICACAO");
        DynamicVO natCcrVO = JapeFactory.dao("TGFNATCCCR").findOne("CODNAT = ? AND NUCLASSIFICACAO = ?",new Object[]{codNat, nuClassificassao});
        if ("N".equals(bemVO.asString("AD_CONTABEM")) || bemVO.asString("AD_CONTABEM").equals((Object)null)) {
            if (natCcrVO != null) {
                BigDecimal codConta = natCcrVO.asBigDecimal("CODCTACTB");
                ((FluidUpdateVO)JapeFactory.dao("Conta").prepareToUpdateByPK(new Object[]{codprod, "D", codbem}).set("CODCTACTB", codConta)).update();
            } else {
                ErroUtils.disparaErro("Não foi possível definir a conta contábil.");
            }
        }

    }

    private void ativaDesativaCredPisCofinsDep(BigDecimal codigoProduto, String codigoBem, String temCredPisCofinsDepr) throws Exception {
        if ("N".equals(temCredPisCofinsDepr)) {
            this.excluirConta(codigoProduto, codigoBem, "3");
            this.excluirConta(codigoProduto, codigoBem, "4");
            this.excluirConta(codigoProduto, codigoBem, "5");
            this.excluirConta(codigoProduto, codigoBem, "6");
        } else {
            this.insereConta(codigoProduto, codigoBem, "3");
            this.insereConta(codigoProduto, codigoBem, "4");
            this.insereConta(codigoProduto, codigoBem, "5");
            this.insereConta(codigoProduto, codigoBem, "6");
        }

    }

    private void excluirConta(BigDecimal codigoProduto, String codigoBem, String tipoConta) throws Exception {
        JapeWrapper contaBemDAO = JapeFactory.dao("Conta");
        JapeWrapper produtoDAO = JapeFactory.dao("Produto");
        DynamicVO produtoVO = produtoDAO.findByPK(new Object[]{codigoProduto});
        DynamicVO contaBemVO = contaBemDAO.findOne("CODPROD = ? AND CODBEM = ? AND TIPO = ?", new Object[]{codigoProduto, codigoBem, tipoConta});
        BigDecimal contaProduto = produtoVO.asBigDecimalOrZero("AD_CODCTACTBBEMTIPO" + tipoConta);
        BigDecimal contaBem = BigDecimal.ZERO;
        if (contaBemVO != null) {
            contaBem = contaBemVO.asBigDecimalOrZero("CODCTACTB");
        }

        if (contaBemVO != null) {
            if (BigDecimal.ZERO.equals(contaProduto)) {
                ((FluidUpdateVO)produtoDAO.prepareToUpdate(produtoVO).set("AD_CODCTACTBBEMTIPO" + tipoConta, contaBem)).update();
            }

            contaBemDAO.deleteByCriteria("CODPROD = ? AND CODBEM = ? AND TIPO = ?", new Object[]{codigoProduto, codigoBem, tipoConta});
        }
    }

    private void insereConta(BigDecimal codigoProduto, String codigoBem, String tipoConta) throws Exception {
        JapeWrapper contaBemDAO = JapeFactory.dao("Conta");
        JapeWrapper produtoDAO = JapeFactory.dao("Produto");
        DynamicVO produtoVO = produtoDAO.findByPK(new Object[]{codigoProduto});
        DynamicVO contaBemVO = contaBemDAO.findOne("CODPROD = ? AND CODBEM = ? AND TIPO = ?", new Object[]{codigoProduto, codigoBem, tipoConta});
        BigDecimal contaProduto = produtoVO.asBigDecimalOrZero("AD_CODCTACTBBEMTIPO" + tipoConta);
        BigDecimal contaBem = BigDecimal.ZERO;
        if (contaBemVO != null) {
            contaBem = contaBemVO.asBigDecimalOrZero("CODCTACTB");
        }

        if (contaBemVO == null) {
            FluidCreateVO contaBemFCVO = contaBemDAO.create();
            contaBemFCVO.set("CODPROD", codigoProduto);
            contaBemFCVO.set("CODBEM", codigoBem);
            contaBemFCVO.set("TIPO", tipoConta);
            contaBemFCVO.set("CODCTACTB", contaProduto);
            if (!"3".equals(tipoConta) && !"5".equals(tipoConta)) {
                if ("4".equals(tipoConta) || "6".equals(tipoConta)) {
                    contaBemFCVO.set("CODHISTCTB", new BigDecimal(10));
                }
            } else {
                contaBemFCVO.set("CODHISTCTB", new BigDecimal(9));
            }

            contaBemFCVO.save();
        }

    }

    private String getTemCredPisCofinsDepr(BigDecimal codlot) throws Exception {
        EntityFacade facade = EntityFacadeFactory.getCoreFacade();
        NativeSql sql = new NativeSql(facade.getJdbcWrapper());
        sql.appendSql("SELECT COUNT(*) AS QTD\nFROM TSIEMP, TGFPAR, AD_TGFLOT, TGFSIT \nWHERE AD_TGFLOT.CODSITE = TGFSIT.CODSITE \nAND TGFSIT.AD_CODPARC = TGFPAR.CODPARC \nAND TSIEMP.CODPARC = TGFPAR.CODPARCMATRIZ\nAND AD_TGFLOT.CODLOT = :CODLOT");
        sql.setNamedParameter("CODLOT", codlot);
        ResultSet result = sql.executeQuery();

        String temCredPisCofinsDepr;
        for(temCredPisCofinsDepr = "S"; result.next(); temCredPisCofinsDepr = result.getInt("QTD") > 0 ? "N" : "S") {
        }

        return temCredPisCofinsDepr;
    }

    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        this.alterBem(persistenceEvent);
    }

    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO transferenciaVO = (DynamicVO)persistenceEvent.getVo();
        JapeWrapper transferenciaDAO = JapeFactory.dao("AD_TCIITETRANS");
        String codbem = transferenciaVO.asString("CODBEM");
        BigDecimal codtrans = transferenciaVO.asBigDecimal("CODTRANS");
        BigDecimal codprod = transferenciaVO.asBigDecimal("CODPROD");
        DynamicVO transfVO = transferenciaDAO.findOne("CODBEM =? AND CODPROD=? AND CODTRANS=?", new Object[]{codbem, codprod, codtrans});
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)transferenciaDAO.prepareToUpdate(transfVO).set("CODLOTANT", this.codlotant)).set("MATRICULAANT", this.matriculaant)).set("MATRICULANOMEANT", this.matriculanomeant)).set("CODCENCUSANT", this.codcencusant)).set("CODLOCANT", this.codlocant)).update();
    }

    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO transferenciaVO = (DynamicVO)persistenceEvent.getVo();
        JapeWrapper bemDAO = JapeFactory.dao("Imobilizado");
        JapeWrapper depDAO = JapeFactory.dao("Departamento");
        BigDecimal codprod = transferenciaVO.asBigDecimal("CODPROD");
        String codbem = transferenciaVO.asString("CODBEM");
        BigDecimal coddepatual = depDAO.findOne("CODCENCUS = ?", new Object[]{transferenciaVO.asBigDecimal("CODCENCUSANT")}).asBigDecimal("CODDEP");
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)bemDAO.prepareToUpdateByPK(new Object[]{codbem, codprod}).set("AD_CODLOT", this.codlotant)).set("AD_MATRICULA", this.matriculaant.toString())).set("AD_MATRICULANOME", this.matriculanomeant)).set("AD_CODLOC", this.codlocant)).update();
    }

    public void beforeCommit(TransactionContext transactionContext) {
    }
}
