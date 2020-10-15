import java.math.BigDecimal;
import java.sql.Timestamp;

public class DadosPlanilhaPOJO {

    BigDecimal documento;
    BigDecimal numeroDoLote;
    Timestamp referencia;
    BigDecimal codigoEmpresa;
    BigDecimal codigoCentroDeCusto;
    BigDecimal codigoNatureza;
    BigDecimal codigoSite;
    BigDecimal codigoContaContabilReduzida;
    BigDecimal codigoProjeto;
    BigDecimal codigoHistoricoPadrao;
    String complementoHistorico;
    String tipoLancamento;
    BigDecimal valor;

    public DadosPlanilhaPOJO() {
    }

    public BigDecimal getDocumento() {
        return this.documento;
    }

    public void setDocumento(BigDecimal documento) {
        this.documento = documento;
    }

    public BigDecimal getNumeroDoLote() {
        return this.numeroDoLote;
    }

    public void setNumeroDoLote(BigDecimal numeroDoLote) {
        this.numeroDoLote = numeroDoLote;
    }

    public Timestamp getReferencia() {
        return this.referencia;
    }

    public void setReferencia(Timestamp referencia) {
        this.referencia = referencia;
    }

    public BigDecimal getCodigoEmpresa() {
        return this.codigoEmpresa;
    }

    public void setCodigoEmpresa(BigDecimal codigoEmpresa) {
        this.codigoEmpresa = codigoEmpresa;
    }

    public BigDecimal getCodigoCentroDeCusto() {
        return this.codigoCentroDeCusto;
    }

    public void setCodigoCentroDeCusto(BigDecimal codigoCentroDeCusto) {
        this.codigoCentroDeCusto = codigoCentroDeCusto;
    }

    public BigDecimal getCodigoNatureza() {
        return this.codigoNatureza;
    }

    public void setCodigoNatureza(BigDecimal codigoNatureza) {
        this.codigoNatureza = codigoNatureza;
    }

    public BigDecimal getCodigoSite() {
        return this.codigoSite;
    }

    public void setCodigoSite(BigDecimal codigoSite) {
        this.codigoSite = codigoSite;
    }

    public BigDecimal getCodigoContaContabilReduzida() {
        return this.codigoContaContabilReduzida;
    }

    public void setCodigoContaContabilReduzida(BigDecimal codigoContaContabilReduzida) {
        this.codigoContaContabilReduzida = codigoContaContabilReduzida;
    }

    public BigDecimal getCodigoHistoricoPadrao() {
        return this.codigoHistoricoPadrao;
    }

    public void setCodigoHistoricoPadrao(BigDecimal codigoHistoricoPadrao) {
        this.codigoHistoricoPadrao = codigoHistoricoPadrao;
    }

    public String getComplementoHistorico() {
        return this.complementoHistorico;
    }

    public void setComplementoHistorico(String complementoHistorico) {
        this.complementoHistorico = complementoHistorico;
    }

    public String getTipoLancamento() {
        return this.tipoLancamento;
    }

    public void setTipoLancamento(String tipoLancamento) {
        this.tipoLancamento = tipoLancamento;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getCodigoProjeto() {
        return this.codigoProjeto;
    }

    public void setCodigoProjeto(BigDecimal codigoProjeto) {
        this.codigoProjeto = codigoProjeto;
    }

    public String toString() {
        return "DadosPlanilhaPOJO{documento=" + this.documento + ", numeroDoLote=" + this.numeroDoLote + ", referencia=" + this.referencia + ", codigoEmpresa=" + this.codigoEmpresa + ", codigoCentroDeCusto=" + this.codigoCentroDeCusto + ", codigoNatureza=" + this.codigoNatureza + ", codigoSite=" + this.codigoSite + ", codigoContaContabilReduzida=" + this.codigoContaContabilReduzida + ", codigoHistoricoPadrao=" + this.codigoHistoricoPadrao + ", complementoHistorico='" + this.complementoHistorico + '\'' + ", tipoLancamento='" + this.tipoLancamento + '\'' + ", valor=" + this.valor + '}';
    }
}
