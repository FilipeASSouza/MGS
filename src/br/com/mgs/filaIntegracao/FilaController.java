package br.com.mgs.filaIntegracao;

import br.com.mgs.utils.DynamicVOUtils;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.util.*;

public class FilaController {
    Map<String, BigDecimal> integrados = new HashMap();
    EntityFacade dwfFacade;

    public FilaController() {
    }

    public static void setFilaIntegracao(String pInstancia, DynamicVO dynamicVO, String pOperacao) throws Exception {
        setFilaIntegracao(pInstancia, dynamicVO, pOperacao, (BigDecimal)null);
    }

    public static void setFilaIntegracao(String pInstancia, DynamicVO dynamicVO, String pOperacao, BigDecimal codTop) throws Exception {
        JapeWrapper filaIntegracaoDao = JapeFactory.dao("TSIFILINT");
        JapeWrapper servicosDao = JapeFactory.dao("TSISRVINT");
        DynamicVO servicoIntegeraco;
        if (codTop != null) {
            servicoIntegeraco = servicosDao.findOne("INSTANCIA = ? AND OPERACAO = ? AND CODTIPOPER = ?", new Object[]{pInstancia, pOperacao, codTop});
        } else if (dynamicVO.containsProperty("CODTIPOPER")) {
            codTop = dynamicVO.asBigDecimal("CODTIPOPER");
            servicoIntegeraco = servicosDao.findOne("INSTANCIA = ? AND OPERACAO = ? AND CODTIPOPER = ?", new Object[]{pInstancia, pOperacao, codTop});
        } else {
            servicoIntegeraco = servicosDao.findOne("INSTANCIA = ? AND OPERACAO = ? AND CODTIPOPER = 0", new Object[]{pInstancia, pOperacao});
        }

        if (servicoIntegeraco != null) {
            boolean integrar = check(dynamicVO, servicoIntegeraco.asString("FILTRO"), pInstancia);
            if (integrar) {
                FluidCreateVO filaIntegracao = filaIntegracaoDao.create();
                String pPrimarykey = dynamicVO.getPrimaryKey().toString();
                String pk = pPrimarykey.replace("PK[", "").replace("]", "");
                filaIntegracao.set("INSTANCIA", pInstancia);
                filaIntegracao.set("CHAVE", pk);
                filaIntegracao.set("DHLANC", TimeUtils.getNow());
                filaIntegracao.set("OPERACAO", pOperacao);
                filaIntegracao.set("CODTIPOPER", codTop);
                filaIntegracao.save();
            }
        } else {
            System.out.println("[INTEGRACAO] Serviço não encontrado");
        }

    }

    public void onTime(ScheduledActionContext scheduledActionContext) {
        this.dwfFacade = EntityFacadeFactory.getDWFFacade();
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            FinderWrapper finder = new FinderWrapper("TSIFILINT", "STATUSPROC IS NULL");
            finder.setOrderBy("DHLANC");
            Collection<DynamicVO> filaVO = this.dwfFacade.findByDynamicFinderAsVO(finder);
            this.integrados.clear();
            ConexaoMsca msca = new ConexaoMsca();
            Iterator var6 = filaVO.iterator();

            while(var6.hasNext()) {
                DynamicVO itemFilaVO = (DynamicVO)var6.next();

                try {
                    msca.loginMGS();
                    String servico = itemFilaVO.asString("TSISRVINT.SERVIOMGS");
                    String filtro = itemFilaVO.asString("TSISRVINT.FILTRO");
                    String chave = itemFilaVO.asString("CHAVE");
                    String instancia = itemFilaVO.asString("INSTANCIA");
                    JapeWrapper instanciaDAO = JapeFactory.dao(instancia);
                    String identificador = instancia + servico + chave;
                    PersistentLocalEntity tsifilint = this.dwfFacade.findEntityByPrimaryKey("TSIFILINT", itemFilaVO.asBigDecimal("NUFILAINTEGRACAO"));
                    EntityVO vo = tsifilint.getValueObject();
                    DynamicVO dynamicVO = (DynamicVO)vo;
                    DynamicVO entidade = instanciaDAO.findByPK(new Object[]{chave});
                    if (check(entidade, filtro, instancia)) {
                        Set<String> strings = this.integrados.keySet();
                        dynamicVO.setProperty("DHPROC", TimeUtils.getNow());
                        if (!strings.contains(identificador)) {
                            msca.sendRequestJson(servico, chave);
                            this.integrados.put(identificador, dynamicVO.asBigDecimal("NUFILAINTEGRACAO"));
                            dynamicVO.setProperty("STATUSPROC", "Concluido");
                        } else {
                            dynamicVO.setProperty("STATUSPROC", "Ignorado - Integrado na fila " + this.integrados.get(identificador));
                        }

                        dynamicVO.setProperty("DHPROCFIN", TimeUtils.getNow());
                        tsifilint.setValueObject(vo);
                    }
                } catch (Exception var23) {
                    var23.printStackTrace();
                    PersistentLocalEntity tsifilint = this.dwfFacade.findEntityByPrimaryKey("TSIFILINT", itemFilaVO.asBigDecimal("NUFILAINTEGRACAO"));
                    EntityVO vo = tsifilint.getValueObject();
                    DynamicVO dynamicVO = (DynamicVO)vo;
                    dynamicVO.setProperty("STATUSPROC", "Erro: " + ExceptionUtils.getStackTrace(var23));
                    dynamicVO.setProperty("DHPROC", TimeUtils.getNow());
                    tsifilint.setValueObject(vo);
                }
            }
        } catch (Exception var24) {
            var24.printStackTrace();
        } finally {
            JapeSession.close(hnd);
        }

    }

    public static boolean check(DynamicVO rootVO, String filtro, String instancia) throws Exception {
        if (filtro == null) {
            return true;
        } else {
            boolean check = DynamicVOUtils.checkFilter(rootVO, filtro, instancia);
            if (!check) {
                System.out.println("[INTEGRACAO] Integração da instancia " + instancia + " ignorada devido ao filtro: " + filtro);
            }

            return check;
        }
    }
}
