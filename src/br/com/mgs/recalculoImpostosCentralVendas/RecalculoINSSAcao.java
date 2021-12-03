package br.com.mgs.recalculoImpostosCentralVendas;

import br.com.mgs.utils.NativeSqlDecorator;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class RecalculoINSSAcao implements AcaoRotinaJava {

    private JdbcWrapper jdbcWrapper = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

    @Override
    public void doAction(ContextoAcao ct) throws Exception {

        JapeWrapper itensDAO = JapeFactory.dao("ItemNota");
        JapeWrapper impostosDAO = JapeFactory.dao("ImpostoItemNota");
        JapeWrapper cabecalhoDAO = JapeFactory.dao("CabecalhoNota");
        JapeWrapper tipoOperacaoDAO = JapeFactory.dao("TipoOperacao"); //BH_REMOVEDESC
        JapeWrapper outrosImpostosItensDAO = JapeFactory.dao("ImpostoNota");


        BigDecimal valorTotalBrutoItem = BigDecimal.ZERO;
        BigDecimal aliquotaINSS = null;
        BigDecimal valorDescontoItem = BigDecimal.ZERO;
        BigDecimal valorImpostosFaturamento = BigDecimal.ZERO;
        BigDecimal valorLiquidoItem = null;
        BigDecimal valorINSS;
        BigDecimal valorNota;

        Registro[] linhas = ct.getLinhas();

        DynamicVO impostoVO = null;

        for(Registro linha : linhas ){

            BigDecimal nunota = (BigDecimal) linha.getCampo("NUNOTA");
            Collection<DynamicVO> itensVO = itensDAO.find("NUNOTA = ?", new Object[]{ nunota });

            DynamicVO tipoOperacaoVO = tipoOperacaoDAO.findOne("CODTIPOPER = ? AND DHALTER = ?"
                    , new Object[]{ linha.getCampo("CODTIPOPER"), linha.getCampo("DHTIPOPER") });

            if( tipoOperacaoVO.asString("BH_REMOVEDESC").equalsIgnoreCase("S")){

                for (DynamicVO itenVO : itensVO ){

                    BigDecimal valorBrutoItem = (BigDecimal) itenVO.asBigDecimal("VLRTOT");
                    //valorTotalBrutoItem.add( valorBrutoItem );
                    valorTotalBrutoItem = valorTotalBrutoItem.add(valorBrutoItem);
                    valorDescontoItem = valorDescontoItem.add( itenVO.asBigDecimalOrZero("VLRDESC"));

                    impostoVO = impostosDAO.findOne("NUNOTA = ? AND SEQUENCIA = ? AND CODIMP = 5 AND CODINC = 0"
                            , new Object[]{ itenVO.asBigDecimal("NUNOTA"), itenVO.asBigDecimal("SEQUENCIA") });

                    // Recuperando a sequencia e a aliquota
                    aliquotaINSS = impostoVO.asBigDecimalOrZero("ALIQUOTA");
                    aliquotaINSS = aliquotaINSS.divide( BigDecimal.valueOf(100L) );

                }

                /*
                    Vlr. líquito da nota = Vlr. Bruto/Valor dos Serviços - Vlr. desconto incondicional - Vlr. INSS - Vlr. IRRF - Vlr. PIS - Vlr. COFINS - Vlr. CSLL
                    INSS não pode arredondar só trunca o valor do INSS na tabela de impostos e na cab
                 */

                NativeSqlDecorator valorLiquidoItemSQL = new NativeSqlDecorator("SELECT ( CASE\n" +
                        "           WHEN CAB.TIPMOV IN ('O', 'C', 'E')\n" +
                        "              THEN TGFITE.VLRTOT - TGFITE.VLRDESC - TGFITE.VLRREPRED\n" +
                        "           ELSE (CASE\n" +
                        "                    WHEN NULLVALUE ((SELECT INTEIRO\n" +
                        "                                 FROM TSIPAR\n" +
                        "                                WHERE CHAVE = 'TIPTABPRECOS'), 0) <> 9\n" +
                        "                       THEN TGFITE.VLRTOT - TGFITE.VLRDESC - TGFITE.VLRREPRED\n" +
                        "                    WHEN NULLVALUE ((SELECT LOGICO\n" +
                        "                            FROM TSIPAR\n" +
                        "                           WHERE CHAVE = 'ADIMPVLRTOTLIQ'), 'S') <> 'S'\n" +
                        "                       THEN TGFITE.VLRTOT - TGFITE.VLRDESC - TGFITE.VLRREPRED\n" +
                        "                    ELSE   (TGFITE.VLRTOT - TGFITE.VLRDESC - TGFITE.VLRREPRED\n" +
                        "                           )\n" +
                        "                         + TGFITE.VLRSUBST\n" +
                        "                         + ((CASE\n" +
                        "                                WHEN TGFITE.VLRIPI > 0\n" +
                        "                                AND NULLVALUE ((SELECT SOMARIPI\n" +
                        "                                            FROM TGFTOP TP\n" +
                        "                                           WHERE TP.CODTIPOPER =\n" +
                        "                                                                CAB.CODTIPOPER\n" +
                        "                                             AND TP.DHALTER = CAB.DHTIPOPER),\n" +
                        "                                         'N'\n" +
                        "                                        ) <> 'N'\n" +
                        "                                   THEN TGFITE.VLRIPI\n" +
                        "                                ELSE 0\n" +
                        "                             END\n" +
                        "                            )\n" +
                        "                           )\n" +
                        "                 END\n" +
                        "                )\n" +
                        "        END\n" +
                        "       ) AS VLRLIQ\n" +
                        "  FROM TGFCAB CAB\n" +
                        "  INNER JOIN TGFITE ON TGFITE.NUNOTA = CAB.NUNOTA " +
                        " WHERE CAB.NUNOTA = :NUNOTA", jdbcWrapper );
                valorLiquidoItemSQL.setParametro("NUNOTA", (BigDecimal) linha.getCampo("NUNOTA"));

                if(valorLiquidoItemSQL.proximo()){
                    valorLiquidoItem = valorLiquidoItemSQL.getValorBigDecimal("VLRLIQ");
                }

                valorLiquidoItemSQL.close();

                DynamicVO cabecalhoVO = cabecalhoDAO.findOne("NUNOTA = ?"
                        , new Object[]{ linha.getCampo("NUNOTA") });

                valorINSS = valorTotalBrutoItem.multiply(aliquotaINSS).setScale(2, RoundingMode.DOWN );

                //Recuperando os valores de impostos do faturamento

                Collection <DynamicVO> outrosImpostosItensVO = outrosImpostosItensDAO.find("NUNOTA = ?"
                        , new Object[]{ linha.getCampo("NUNOTA") } );

                for( DynamicVO outrosImpostosIten : outrosImpostosItensVO ){
                    valorImpostosFaturamento = valorImpostosFaturamento.add(outrosImpostosIten.asBigDecimalOrZero("VALOR"));
                }

                valorNota = valorLiquidoItem.subtract((BigDecimal) linha.getCampo("VLRIRF")).subtract(valorINSS).subtract(valorImpostosFaturamento).setScale( 2, RoundingMode.HALF_EVEN );

                // verificar se o valor bruto da nota está igual ao valor bruto dos itens

                if( valorTotalBrutoItem != (BigDecimal) linha.getCampo("VLRNOTA") ){


                    NativeSqlDecorator updateCentralSQL = new NativeSqlDecorator("UPDATE TGFCAB " +
                            " SET VLRINSS = :VLRINSS " +
                            " , BASEINSS = :BASEINSS " +
                            " , VLRNOTA = :VLRNOTA " +
                            " WHERE NUNOTA = :NUNOTA", jdbcWrapper );
                    updateCentralSQL.setParametro("VLRINSS", valorINSS );
                    updateCentralSQL.setParametro("BASEINSS", valorTotalBrutoItem );
                    updateCentralSQL.setParametro("VLRNOTA", valorNota );
                    updateCentralSQL.setParametro("NUNOTA", nunota );
                    updateCentralSQL.atualizar();

                    updateCentralSQL.close();

                    NativeSqlDecorator updateFinanceiroSQL = new NativeSqlDecorator(" UPDATE TGFFIN " +
                            " SET VLRDESDOB = :VLRDESDOB " +
                            " WHERE NUNOTA = :NUNOTA ", jdbcWrapper );
                    updateFinanceiroSQL.setParametro("VLRDESDOB", valorNota );
                    updateFinanceiroSQL.setParametro("NUNOTA", nunota );
                    updateFinanceiroSQL.atualizar();

                    updateFinanceiroSQL.close();


                    NativeSqlDecorator updateImpostoINSSSQL = new NativeSqlDecorator(" UPDATE TGFDIN " +
                            " SET VALOR = :VALOR, BASE = :BASE, BASERED = :BASERED " +
                            " WHERE NUNOTA = :NUNOTA AND SEQUENCIA = 1 " +
                            " AND CODIMP = 5 AND CODINC = 0 ", jdbcWrapper );
                    updateImpostoINSSSQL.setParametro("VALOR", valorINSS );
                    updateImpostoINSSSQL.setParametro("BASE", valorTotalBrutoItem );
                    updateImpostoINSSSQL.setParametro("BASERED", valorTotalBrutoItem );
                    updateImpostoINSSSQL.setParametro("NUNOTA", nunota );
                    updateImpostoINSSSQL.atualizar();

                    updateImpostoINSSSQL.close();

                    /*
                    Atualizando os campos:

                    FINANCEIRO
                    - VLRDESDOB

                    CABECALHO
                    - VLRNOTA, BASEINSS, VLRINSS

                    IMPOSTOS
                    -BASE, BASERED, VALOR
                     */
                }
            }
        }
    }
}
