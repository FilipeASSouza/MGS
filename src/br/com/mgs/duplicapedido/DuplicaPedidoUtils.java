package br.com.mgs.duplicapedido;

import br.com.sankhya.bh.dao.DynamicVOKt;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DuplicaPedidoUtils {
    public DuplicaPedidoUtils() {
    }

    public static void duplicaPeloCorte(BigDecimal nuNota) throws Exception {

        JapeWrapper itensnotaDAO = JapeFactory.dao("ItemNota");
        JapeWrapper notaDAO = JapeFactory.dao("CabecalhoNota");
        JapeWrapper rateioDAO = JapeFactory.dao("RateioRecDesp");
        JapeWrapper varDAO = JapeFactory.dao("CompraVendavariosPedido");
        DynamicVO notaOrigemVO = notaDAO.findByPK(nuNota);
        Map<String, Object> campos = new HashMap();
        campos.put("DTNEG", notaOrigemVO.asTimestamp("DTNEG"));
        campos.put("ORDEMCARGA", (Object)null);
        DynamicVO notaDestino = DynamicVOKt.duplicaRegistro(notaOrigemVO, campos);
        Collection<DynamicVO> itens = itensnotaDAO.find("NUNOTA = ?", new Object[]{nuNota});
        Iterator var9 = itens.iterator();

        while(var9.hasNext()) {
            DynamicVO item = (DynamicVO)var9.next();
            if (item.asBigDecimal("QTDNEG").compareTo(item.asBigDecimalOrZero("QTDWMS")) > 0) {
                BigDecimal qtdNeg = item.asBigDecimal("QTDNEG").subtract(item.asBigDecimalOrZero("QTDWMS"));
                FluidUpdateVO itemUPD = itensnotaDAO.prepareToUpdateByPK(nuNota, item.asBigDecimal("SEQUENCIA"));
                ((FluidUpdateVO)itemUPD.set("QTDCONFERIDA", qtdNeg)).update();
                FluidCreateVO itemNota = itensnotaDAO.create();
                itemNota.set("NUNOTA", notaDestino.asBigDecimal("NUNOTA"));
                itemNota.set("CODPROD", item.asBigDecimal("CODPROD"));
                itemNota.set("QTDNEG", qtdNeg);
                itemNota.set("ATUALESTOQUE", item.asBigDecimal("ATUALESTOQUE"));
                itemNota.set("RESERVA", item.asString("RESERVA"));
                itemNota.set("CODLOCALORIG", item.asBigDecimal("CODLOCALORIG"));
                itemNota.set("STATUSNOTA", "A");
                itemNota.set("USOPROD", item.asString("USOPROD"));
                itemNota.set("CONTROLE", item.asString("CONTROLE"));
                itemNota.set("CODVOL", item.asString("CODVOL"));
                itemNota.set("VLRUNIT", item.asBigDecimal("VLRUNIT"));
                itemNota.set("VLRTOT", qtdNeg.multiply(item.asBigDecimal("VLRUNIT")));
                DynamicVO itemNotaVO = itemNota.save();
                (varDAO.create().set("NUNOTA", notaDestino.asBigDecimal("NUNOTA"))).set("NUNOTAORIG", notaOrigemVO.asBigDecimal("NUNOTA")).set("SEQUENCIA", itemNotaVO.asBigDecimal("SEQUENCIA")).set("SEQUENCIAORIG", item.asBigDecimal("SEQUENCIA")).set("QTDATENDIDA", qtdNeg).set("STATUSNOTA", "A").save();
            }
        }

        Collection<DynamicVO> rateios = rateioDAO.find("NUFIN = ? AND ORIGEM = 'E'", new Object[]{nuNota});
        Iterator var16 = rateios.iterator();

        while(var16.hasNext()) {
            DynamicVO rateio = (DynamicVO)var16.next();
            (JapeFactory.dao("RateioRecDesp").create().set("ORIGEM", "E")).set("NUFIN", notaDestino.asBigDecimal("NUNOTA")).set("CODNAT", rateio.asBigDecimal("CODNAT")).set("CODCENCUS", rateio.asBigDecimal("CODCENCUS")).set("CODPROJ", rateio.asBigDecimal("CODPROJ")).set("CODSITE", rateio.asBigDecimal("CODSITE")).set("CODPARC", rateio.asBigDecimal("CODPARC")).set("PERCRATEIO", rateio.asBigDecimal("PERCRATEIO")).save();
        }
        (notaDAO.prepareToUpdateByPK(new Object[]{nuNota}).set("AD_NUNOTAPED", notaDestino.asBigDecimal("NUNOTA"))).update();

        BigDecimal nunotaDest = notaDestino.asBigDecimal("NUNOTA");

        if(notaDestino.asBigDecimal("NUNOTA") != null) {
            (notaDAO.prepareToUpdateByPK(new Object[]{nunotaDest}).set("AD_NUNOTAPED", "")).update();
        }
    }
}
