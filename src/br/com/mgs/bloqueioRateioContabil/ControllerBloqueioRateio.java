package br.com.mgs.bloqueioRateioContabil;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

import java.math.BigDecimal;

public class ControllerBloqueioRateio {

    public ControllerBloqueioRateio() {
    }

    public static Boolean validaRateioNota(BigDecimal nunota) throws Exception {
        DynamicVO cabVO = JapeFactory.dao("CabecalhoNota").findByPK(nunota);
        JapeWrapper fechaDAO = JapeFactory.dao("BHBLCFechamentoRotinas");
        DynamicVO empVO = fechaDAO.findOne("CODTIPOPER = ?", new Object[]{cabVO.asBigDecimal("CODTIPOPER")});
        DynamicVO tipVO = fechaDAO.findOne("TIPMOV = ?", new Object[]{cabVO.asString("TIPMOV")});
        if (empVO != null) {
            if ("N".equals(empVO.asString("ATIVO"))) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTN")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(empVO.asString("TIPDTE")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTF")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTM")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        } else {
            if (tipVO == null) {
                return false;
            } else if ("N".equals(tipVO.asString("ATIVO"))) {
                return false;
            } else if ("S".equals(tipVO.asString("TIPDTN")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTE")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTF")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTM")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        }
    }

    public static Boolean validaRateioFinanceiro(BigDecimal nufin) throws Exception {
        DynamicVO finVO = JapeFactory.dao("Financeiro").findByPK(nufin);
        DynamicVO cabVO = JapeFactory.dao("CabecalhoNota").findByPK(finVO.asBigDecimal("NUNOTA"));
        JapeWrapper fechaDAO = JapeFactory.dao("BHBLCFechamentoRotinas");
        DynamicVO empVO = fechaDAO.findOne("CODTIPOPER = ?", new Object[]{finVO.asBigDecimal("CODTIPOPER")});
        DynamicVO tipVO = fechaDAO.findOne("TIPMOV = ?", new Object[]{cabVO.asString("TIPMOV")});
        if (empVO != null) {
            if ("N".equals(empVO.asString("ATIVO"))) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTN")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(empVO.asString("TIPDTE")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTF")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTM")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        } else {
            if (tipVO == null) {
                return false;
            } else if ("N".equals(tipVO.asString("ATIVO"))) {
                return false;
            } else if ("S".equals(tipVO.asString("TIPDTN")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTE")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTF")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTM")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        }
    }

    public static Boolean validaRateioBaixa(BigDecimal nufin) throws Exception {
        DynamicVO finVO = JapeFactory.dao("Financeiro").findByPK(nufin);
        DynamicVO cabVO = JapeFactory.dao("CabecalhoNota").findByPK(finVO.asBigDecimal("NUNOTA"));
        JapeWrapper fechaDAO = JapeFactory.dao("BHBLCFechamentoRotinas");
        DynamicVO empVO = fechaDAO.findOne("CODTIPOPER = ?", new Object[]{finVO.asBigDecimal("CODTIPOPERBAIXA")});
        DynamicVO tipVO = fechaDAO.findOne("TIPMOV = ?", new Object[]{cabVO.asString("TIPMOV")});
        if (empVO != null) {
            if ("N".equals(empVO.asString("ATIVO"))) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTN")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(empVO.asString("TIPDTE")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTF")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            }else if ("S".equals(empVO.asString("TIPDTM")) && empVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        } else {
            if (tipVO == null) {
                return false;
            } else if ("N".equals(tipVO.asString("ATIVO"))) {
                return false;
            } else if ("S".equals(tipVO.asString("TIPDTN")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTNEG")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTE")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTENTSAI")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTF")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTFATUR")) > 0) {
                return true;
            } else if ("S".equals(tipVO.asString("TIPDTM")) && tipVO.asTimestamp("DATA").compareTo(cabVO.asTimestamp("DTMOV")) > 0) {
                return true;
            }
            return false;

        }
    }
}
