package br.com.mgs.utils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * Desenvolvido por Fernando Lopes Sankhya Unidade BH
 * Versao 1.0
 * suporte a xls, xlsx
 * */
public class LerArquivoDeDadosDecorator {
    private String arquivo;
    private String extensao;
    private int numeroDeLinhas = 0;
    private int linhaSelecionada = 0;
    private boolean aberto = false;
    private Map<String, Integer> mapaDeColunas = new HashMap<String, Integer>();

    private HSSFSheet planilhaXsl;
    private XSSFSheet planilhaXslx;

    private LerArquivoDeDadosDecorator() {
    }

    public LerArquivoDeDadosDecorator(String arquivo) {
        this.arquivo = arquivo;
        this.extensao = arquivo.substring(arquivo.lastIndexOf(".") + 1, arquivo.length());
    }

    public int getNumeroDeLinhas() {
        return numeroDeLinhas;
    }

    public int getLinhaSelecionada() {
        return linhaSelecionada;
    }

    public LerArquivoDeDadosDecorator(String caminhaAquivo, String extensao) {
        this.arquivo = caminhaAquivo;
        this.extensao = extensao;
    }

    public void setColuna(String nomeColuna, int posicaoColuna) {
        mapaDeColunas.put(nomeColuna, posicaoColuna);
    }

    public void setColuna(String nomeColuna, String coluna) {
        char caracter = coluna.toUpperCase().charAt(0);
        int codigo = (int) caracter;
        int PosicaoColuna = codigo - 65;

        mapaDeColunas.put(nomeColuna, PosicaoColuna);
    }

    public boolean proximo() throws Exception {
        if (!aberto) {
            abrirArquivo();
            aberto = true;
        }
        linhaSelecionada++;
        return linhaSelecionada < numeroDeLinhas;
    }

    private void abrirArquivo() throws Exception {
        switch (extensao.toLowerCase()) {
            case "xls":
                abrirArquivoXls();
                break;
            case "xlsx":
                abrirArquivoXslx();
                break;
        }
    }

    private void abrirArquivoXls() throws Exception {
        FileInputStream file = new FileInputStream(new File(arquivo));
        //abre pasta de trabalho
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(file);
        //abre planilha
        planilhaXsl = hssfWorkbook.getSheetAt(0);

        numeroDeLinhas = planilhaXsl.getPhysicalNumberOfRows();
    }

    private void abrirArquivoXslx() throws Exception {
        FileInputStream file = new FileInputStream(new File(arquivo));
        //abre pasta de trabalho
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        //abre planilha
        planilhaXslx = xssfWorkbook.getSheetAt(0);

        numeroDeLinhas = planilhaXslx.getPhysicalNumberOfRows();
    }

    private Cell getCell(String campo) throws Exception {
        Cell cell = null;
        switch (extensao.toLowerCase()) {
            case "xls":
                cell = planilhaXsl.getRow(linhaSelecionada).getCell(mapaDeColunas.get(campo));
                break;
            case "xlsx":
                cell = planilhaXslx.getRow(linhaSelecionada).getCell(mapaDeColunas.get(campo));
                break;
        }
        return cell;
    }


    public BigDecimal getValorBigDecimal(String campo) throws Exception {
        try {
        BigDecimal valor = null;
        Cell cell = getCell(campo);
        if (cell == null)
            return null;

        if (cell.toString().trim().equals(""))
            return null;

        valor = new BigDecimal(cell.getNumericCellValue());
        return valor;
        } catch (Exception e) {
            throw new Exception("Linha "+linhaSelecionada+" Campo " + campo + ": " + e);
        }
    }

    public BigDecimal getValorBigDecimal2(String campo) throws Exception {
        try {
            BigDecimal valor = null;
            Cell cell = getCell(campo);
            if (cell == null)
                return null;

            if (cell.toString().trim().equals(""))
                return null;

            valor = BigDecimal.valueOf(cell.getNumericCellValue());
            return valor;
        } catch (Exception e) {
            throw new Exception("Linha "+linhaSelecionada+" Campo " + campo + ": " + e);
        }
    }


    public String getValorString(String campo) throws Exception {
        try {
            String valor = null;
            Cell cell = getCell(campo);
            if (cell == null) {
                return null;
            }
            valor = cell.getStringCellValue();
            return valor;
        } catch (Exception e) {
            throw new Exception("Linha "+linhaSelecionada+" Campo " + campo + ": " + e);
        }
    }

    public Timestamp getValorTimestamp(String campo) throws Exception {
        try {
            Timestamp valor = null;
            Cell cell = getCell(campo);
            if (cell == null) {
                return null;
            }
            Date dateCellValue = cell.getDateCellValue();

            if (dateCellValue == null){
                return null;
            }

            valor = new Timestamp(dateCellValue.getTime());
            return valor;
        } catch (Exception e) {
            throw new Exception("Linha "+linhaSelecionada+" Campo " + campo + ": " + e);
        }
    }
}
