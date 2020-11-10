package br.com.mgs.utils;

import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

/*
* Desenvolvido por Fernando Lopes Sankhya Unidade BH
* Versao 1.5
* */
public class NativeSqlDecorator {
    private NativeSql nativeSql;
    private String sql;
    private boolean aberto = false;
    ResultSet resultSet;



    private NativeSqlDecorator() {

    }

    public NativeSqlDecorator(String sql){
        iniciar();
        nativeSql.appendSql(sql);
    }


    public boolean proximo() throws Exception {
        if (!aberto){
            executar();
            aberto = true;
        }
        return resultSet.next();
    }

    public boolean loop() throws Exception {
        return proximo();
    }


    public NativeSqlDecorator(Object objetobase, String arquivo) throws Exception {
        iniciar();

        //nativeSql.appendSql(getSqlResource(objetobase, arquivo));
        nativeSql.loadSql(objetobase.getClass(), arquivo);
    }

    public NativeSqlDecorator setParametro(String nome, Object valor){
        nativeSql.setNamedParameter(nome, valor);
        return this;
    }

    public BigDecimal getValorBigDecimal(String campo) throws Exception {
        return resultSet.getBigDecimal(campo);
    }

    public String getValorString(String campo) throws Exception {
        return resultSet.getString(campo);
    }

    private Boolean getValorBoolean(String campo) throws Exception {
        return resultSet.getBoolean(campo);
    }

    public Timestamp getValorTimestamp(String campo) throws Exception {
        return resultSet.getTimestamp(campo);
    }

    public int getValorInt(String campo) throws Exception {
        return resultSet.getInt(campo);
    }

    private float getValorFloat(String campo) throws Exception {
        return resultSet.getFloat(campo);
    }


    private void iniciar(){
        nativeSql = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
    }

    public void executar() throws Exception {
        resultSet = nativeSql.executeQuery();
        if (resultSet != null ){
            aberto = true;
        }
    }

    public void atualizar() throws Exception {
        nativeSql.executeUpdate();

    }


    private String getSqlResource(Object objetobase, String arquivo) throws Exception {
        InputStream in = objetobase.getClass().getResourceAsStream(arquivo);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer buf = new StringBuffer(512);
        String line = null;

        while((line = reader.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }

        return buf.toString();
    }
}
