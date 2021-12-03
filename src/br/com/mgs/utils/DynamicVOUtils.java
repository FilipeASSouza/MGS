package br.com.mgs.utils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.DynamicVOPojo;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.io.StringReader;

public class DynamicVOUtils {
    public DynamicVOUtils() {
    }

    public static Object getTypedValue(EntityFacade entityFacade, String instancia, String campo, String value) throws Exception {
        EntityDAO rootDAO = entityFacade.getDAOInstance(instancia);
        Object typedValue = rootDAO.getFieldTypedValue(value, campo);
        return typedValue;
    }

    public static void main(String[] args) throws Exception {
        DynamicVO notaVO = new DynamicVOPojo();
        notaVO.setProperty("STATUSNFSE", "S");
        boolean parceiro = checkFilter(notaVO, "CLIENTE ='S'", "Parceiro");
        System.out.println(parceiro);
    }

    public static boolean checkFilter(DynamicVO entidade, String filtro, String instancia) throws Exception {
        if (filtro == null) {
            return true;
        } else {
            CCJSqlParserManager pm = new CCJSqlParserManager();
            String sql = "SELECT * FROM DUAL WHERE " + filtro;
            Select statement = (Select)pm.parse(new StringReader(sql));
            Where w = new Where();
            boolean test = w.test(entidade, statement);
            return test;
        }
    }

    private static class Where implements SelectVisitor, ExpressionVisitor {
        DynamicVO vo;
        boolean test;

        private Where() {
            this.test = true;
        }

        public boolean test(DynamicVO vo, Select select) {
            this.vo = vo;
            select.getSelectBody().accept(this);
            return this.test;
        }

        public void visit(NullValue nullValue) {
            System.out.println(nullValue);
        }

        public void visit(Function function) {
            System.out.println(function);
        }

        public void visit(InverseExpression inverseExpression) {
            System.out.println(inverseExpression);
        }

        public void visit(JdbcParameter jdbcParameter) {
            System.out.println(jdbcParameter);
        }

        public void visit(DoubleValue doubleValue) {
            System.out.println(doubleValue);
        }

        public void visit(LongValue longValue) {
            System.out.println(longValue);
        }

        public void visit(DateValue dateValue) {
            System.out.println(dateValue);
        }

        public void visit(TimeValue timeValue) {
            System.out.println(timeValue);
        }

        public void visit(TimestampValue timestampValue) {
            System.out.println(timestampValue);
        }

        public void visit(Parenthesis parenthesis) {
            System.out.println(parenthesis);
        }

        public void visit(StringValue stringValue) {
            System.out.println(stringValue);
        }

        public void visit(Addition addition) {
            System.out.println(addition);
        }

        public void visit(Division division) {
            System.out.println(division);
        }

        public void visit(Multiplication multiplication) {
            System.out.println(multiplication);
        }

        public void visit(Subtraction subtraction) {
        }

        public void visit(AndExpression andExpression) {
        }

        public void visit(OrExpression orExpression) {
        }

        public void visit(Between between) {
        }

        public void visit(EqualsTo equalsTo) {
            if (this.vo != null && this.test) {
                String campo = equalsTo.getLeftExpression().toString();
                String valor = equalsTo.getRightExpression().toString();
                valor = valor.replace("'", "");
                Object property = this.vo.getProperty(campo);
                if (!valor.equals(property)) {
                    this.test = false;
                }
            }

        }

        public void visit(GreaterThan greaterThan) {
        }

        public void visit(GreaterThanEquals greaterThanEquals) {
        }

        public void visit(InExpression inExpression) {
        }

        public void visit(IsNullExpression isNullExpression) {
        }

        public void visit(LikeExpression likeExpression) {
        }

        public void visit(MinorThan minorThan) {
        }

        public void visit(MinorThanEquals minorThanEquals) {
        }

        public void visit(NotEqualsTo notEqualsTo) {
        }

        public void visit(Column column) {
        }

        public void visit(SubSelect subSelect) {
        }

        public void visit(CaseExpression caseExpression) {
        }

        public void visit(WhenClause whenClause) {
        }

        public void visit(ExistsExpression existsExpression) {
        }

        public void visit(AllComparisonExpression allComparisonExpression) {
        }

        public void visit(AnyComparisonExpression anyComparisonExpression) {
        }

        public void visit(Concat concat) {
        }

        public void visit(Matches matches) {
        }

        public void visit(BitwiseAnd bitwiseAnd) {
        }

        public void visit(BitwiseOr bitwiseOr) {
        }

        public void visit(BitwiseXor bitwiseXor) {
        }

        public void visit(PlainSelect plainSelect) {
            plainSelect.getWhere().accept(this);
        }

        public void visit(Union union) {
        }
    }
}
