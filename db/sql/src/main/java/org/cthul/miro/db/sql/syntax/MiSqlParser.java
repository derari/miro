package org.cthul.miro.db.sql.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.SqlBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.SqlJoinableClause;
import org.cthul.miro.db.sql.SqlTableClause;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.function.MiFunction;


/**
 *
 */
public class MiSqlParser {
    
    public static QlCode parseCode(String expression) {
        return new MiSqlParser(expression).parseCode();
    }
    
    public static Attribute parseAttribute(String attribute) {
        return new MiSqlParser(attribute).parseAttribute();
    }
    
    public static List<Attribute> parseAttributes(String attributes) {
        return new MiSqlParser(attributes).parseAttributes();
    }
    
    public static SelectPart parseSelectPart(String selectPart) {
        return new MiSqlParser(selectPart).parseSelectPart();
    }
    
    public static TablePart parseFromPart(String fromPart) {
        return new MiSqlParser(fromPart).parseFromPart();
    }
    
    public static JoinPart parseJoinPart(String joinPart) {
        return new MiSqlParser(joinPart).parseJoinPart();
    }
    
    public static SelectStmt parsePartialSelect(String sql) {
        return new MiSqlParser(sql).parsePartialSelectStmt(null);
    }
    
    public static void parsePartialSelectOrCode(String sql, String defaultPart, SelectBuilder stmtBuilder, QlBuilder<?> qlBuilder) {
        new MiSqlParser(sql).parsePartialSelectStmtOrCode(defaultPart, stmtBuilder, qlBuilder);
    }

    private final String input;
    private int cIndex, nIndex, index = 0;
    private Token current = null;
    private Token next = null;
    private int expectIndex = 0;
    private final Set<String> expected = new LinkedHashSet<>();

    public MiSqlParser(String input) {
        this.input = input;
    }
    
    protected boolean expected(String e) {
        return expected(e, cIndex);
    }
    
    protected boolean expected(String e, int index) {
        if (index > expectIndex) {
            expected.clear();
            expectIndex = index;
        }
        if (index == expectIndex) {
            expected.add(e);
        }
        return false;
    }
    
    protected RuntimeException unexpectedException() {
        int i = expectIndex;
        String s = input.substring(Math.max(0, i-10), Math.min(i, input.length())) + 
                "^" +
                (i >= input.length()? "[End]" : input.substring(i, Math.min(i+20, input.length())));
        return new IllegalStateException("Expected " + expected + " at: " + s);
    }
    
    protected void requireAtEnd() {
        if (!atEnd()) {
            expected("<EOI>");
            throw unexpectedException();
        }
    }
    
    protected boolean atEnd() {
        return current().isEnd();
    }
    
    protected void setIndex(int i) {
        if (this.index != i) {
            this.index = i;
            current = next = null;
        }
    }
    
    protected Token current() {
        if (current == null) return next();
        return current;
    }
    
    protected Token next() {
        current = peek();
        cIndex = nIndex;
        next = null;
        return current;
    }
    
    protected Token peek() {
        if (next != null) return next;
        whitespace();
        nIndex = index;
        return next = readNextToken();
    }
    
    protected Token readNextToken() {
        if (index == input.length()) return T_AT_END;
        char c = input.charAt(index);
        if (c == '\'') return stringLiteral();
        if (c == '`') return quotedName();
        if (Character.isJavaIdentifierStart(c)) return word();
        if (Character.isDigit(c)) return number();
        if (isOperator(c)) return operator();
        if (c == '?' || isSpecial(c)) {
            index++;
            String s = ""+c;
            return new Token(s, QlCode.ql(s), c == '?' ? TokenType.VALUE : TokenType.SPECIAL);
        }
        return T_ERROR;
    }
    
    protected String quoted(char q) {
        int start = ++index;
        if (index >= input.length()) {
            expected("Closing "+q, index);
            return null;
        }
        char c = input.charAt(start);
        while (c != q) {
            if (c == '\\') index++;
            if (++index >= input.length()) {
                expected("Closing "+q, start);
                return null;
            }
            c = input.charAt(index);
        }
        index++;
        return input.substring(start, index-1).replace("\\"+q, ""+q);
    }
    
    protected boolean whitespace() {
        if (index >= input.length()) return false;
        int start = index;
        int c = input.codePointAt(index);
        while (Character.isWhitespace(c)) {
            index += Character.charCount(c);
            if (index >= input.length()) break;
            c = input.codePointAt(index);            
        }
        return start < index;
    }
    
    protected Token stringLiteral() {
        String s = quoted('\'');
        if (s == null) return T_ERROR;
        return new Token(s, QlCode.str(s), TokenType.VALUE);
    }
    
    protected Token quotedName() {
        String s = quoted('`');
        if (s == null) return T_ERROR;
        return new Token(s, QlCode.id(s), TokenType.IDENTIFIER);
    }
    
    protected Token word() {
        int start = index;
        int c = input.codePointAt(index);
        while (Character.isJavaIdentifierPart(c)) {
            index += Character.charCount(c);
            if (index >= input.length()) break;
            c = input.codePointAt(index);
        }
        String s = input.substring(start, index);
        return new Token(s, QlCode.ql(s), TokenType.WORD);
    }
    
    protected Token number() {
        int start = index;
        int c = input.codePointAt(index);
        while (Character.isDigit(c)) {
            index += Character.charCount(c);
            if (index >= input.length()) break;
            c = input.codePointAt(index);
        }
        if (c == '.') {
            index++;
            while (Character.isDigit(c)) {
                index += Character.charCount(c);
                if (index >= input.length()) break;
                c = input.codePointAt(index);
            }
        }
        String s = input.substring(start, index);
        return new Token(s, QlCode.ql(s), TokenType.VALUE);
    }
    
    private static final char[] SPECIALS = "(),.;".toCharArray();
    private static final char[] OPERATOR = "!=<>|&+-*/%".toCharArray();
    static { Arrays.sort(SPECIALS); Arrays.sort(OPERATOR); }
    
    private static boolean isSpecial(char c) {
        return Arrays.binarySearch(SPECIALS, c) >= 0;
    }
    
    private static boolean isOperator(char c) {
        return Arrays.binarySearch(OPERATOR, c) >= 0;
    }
    
    protected Token operator() {
        int start = index;
        char c = input.charAt(index);
        while (isOperator(c)) {
            c = input.charAt(++index);
        }
        String s = input.substring(start, index);
        return new Token(s, QlCode.ql(s), TokenType.OPERATOR);
    }
    
    protected boolean atKeyword() {
        return current().isKeyword() && !peek().isSpecial(".");
    }
    
    protected boolean atKeyword(String s) {
        return current().isWord(s) && !peek().isSpecial(".");
    }
    
    protected boolean readKeyword(String s) {
        if (atKeyword(s)) {
            next();
            return true;
        }
        return false;
    }
    
    protected Expression expression() {
        List<ObjectRef> columns = new ArrayList<>();
        QlCode.Builder code = QlCode.build();
        if (!expression(columns, code)) {
            return null;
        }
        return new Expression(columns, code);
    }
    
    protected Expression expressionList() {
        List<ObjectRef> columns = new ArrayList<>();
        QlCode.Builder code = QlCode.build();
        if (!expression_list(columns, code)) {
            return null;
        }
        return new Expression(columns, code);
    }
    
    protected Expression expressionValue() {
        List<ObjectRef> columns = new ArrayList<>();
        QlCode.Builder code = QlCode.build();
        if (!expression_value(columns, code)) {
            return null;
        }
        return new Expression(columns, code);
    }
    
    protected boolean expression_list(List<ObjectRef> columns, QlCode.Builder code) {
        if (!expression(columns, code)) return false;
        while (current().isSpecial(",")) {
            int i = cIndex;
            code.append(", ");
            next();
            if (!expression(columns, code)) {
                setIndex(i);
                break;
            }
        }
        if (!atEnd()) {
            expected("','");
        }
        return true;
    }
    
    protected boolean expression(List<ObjectRef> columns, QlCode.Builder code) {
        return expression_logical(columns, code);
    }
    
    protected boolean expression_logical(List<ObjectRef> columns, QlCode.Builder code) {
        if (!expression_operators(columns, code)) {
            return false;
        }
        while (!atEnd()) {
            Token c = current();
            if (!c.isKeyword()) break;
            if (!c.value.equals("AND") && !c.value.equals("OR")) break;
            int i = cIndex;
            next();
            QlCode.Builder code2 = QlCode.build();
            if (!expression_operators(columns, code2)) {
                setIndex(i);
                return true;
            }
            code.append(" ").append(c.value).append(" ")
                .append(code2);
        }
        return true;
    }
    
    protected boolean expression_operators(List<ObjectRef> columns, QlCode.Builder code) {
        boolean space = expression_words(columns, code);
        while (!atEnd()) {
            Token c = current();
            if (!c.isOperator()) break;
            if (space) code.append(" ");
            code.append(c.code);
            next();
            space = true;
            QlCode.Builder code2 = QlCode.build();
            if (expression_words(columns, code2)) {
                code.append(" ").append(code2);
            }
        }
        return space; // true iff something was matched
    }
    
    protected boolean expression_words(List<ObjectRef> columns, QlCode.Builder code) {
        boolean space = expression_not_words(columns, code);
        while (!atEnd()) {
            Token c = current();
            if (!c.isWord() || c.isKeyword()) break;
            if (space) code.append(" ");
            code.append(c.code);
            next();
            space = true;
            QlCode.Builder code2 = QlCode.build();
            if (expression_not_words(columns, code2)) {
                code.append(" ").append(code2);
            }
        }
        return space; // true iff something was matched
    }
    
    protected boolean expression_not_words(List<ObjectRef> columns, QlCode.Builder code) {
        if (current().isWord() && peek().isSpecial("(")) {
            return false;
        }
        return expression_value(columns, code);
    }
    
    protected boolean expression_value(List<ObjectRef> columns, QlCode.Builder code) {
        if (current().isValue()) {
            code.append(current().code);
            next();
            return true;
        }
        ObjectRef col = objectRef();
        if (col != null) {
            columns.add(col);
            code.append(col.expression);
            return true;
        }
        if (expression_nested(columns, code)) {
            return true;
        }
        return expected("VALUE");
    }
    
    protected boolean expression_nested(List<ObjectRef> columns, QlCode.Builder code) {
        int start = cIndex;
        if (!current().isSpecial("(")) return expected("'('");
        next();
        QlCode.Builder code2 = QlCode.build();
        if (!expression_nested_inner(columns, code2)) {
            setIndex(start);
            return false;
        }
        if (!current().isSpecial(")")) {
            expected("')'");
            setIndex(start);
            return false;
        }
        code.append("(").append(code2).append(")");
        next();
        return true;
    }
    
    protected boolean expression_nested_inner(List<ObjectRef> columns, QlCode.Builder code) {
        return expression_query(columns, code) || expression_list(columns, code);
    }
    
    protected boolean expression_query(List<ObjectRef> columns, QlCode.Builder code) {
        if (!atKeyword("SELECT")) return false;
        boolean space = false;
        while (current().isKeyword()) {
            if (space) code.append(" ");
            code.append(current().code);
            space = true;
            next();
            if (readKeyword("BY")) {
                code.append(" BY");
            }
            QlCode.Builder code2 = QlCode.build();
            if (expression_list(columns, code2)) {
                code.append(" ").append(code2);
            }
        }
        return true;
    }
//        List<ObjectRef> columns2 = new ArrayList<>(); 
//        QlCode.Builder code2 = QlCode.build().append("(");
//        if (!expression(columns2, code2)) {
//            setIndex(start);
//            return false;
//        }
//        while (current().isSpecial(",")) {
//            code2.append(", ");
//            next();
//            if (!expression(columns2, code2)) {
//                setIndex(start);
//                return false;
//            }
//        }
//        if (!current().isSpecial(")")) {
//            setIndex(start);
//            return expected("')'");
//        }
//        next();
//        columns.addAll(columns2);
//        code.append(code2).append(")");
//        return true;
//    }
    
    protected ObjectRef objectRef() {
        Token c = current();
        if (!c.isWordOrIdentifier() || atKeyword()) {
            expected("Identifier");
            return null;
        }
        QlCode.Builder code = QlCode.build(c.code);
        String table = null;
        while (next().isSpecial(".")) {
            table = c.value;
            c = next();
            if (!c.isIdentifier() && !c.isWord()) {
                expected("Identifier");
                return null;
            }
            code.append(".").append(c.code);
        }
        return new ObjectRef(table, c.value, code);
    }
    
    protected QlCode code() {
        List<ObjectRef> columns = new ArrayList<>();
        QlCode.Builder code = QlCode.build();
        QlCode.Builder code2 = QlCode.build();
        boolean space = false;
        while (!atEnd()) {
            if (expression(columns, code2)) {
                if (space) code.append(" ");
                code.append(code2);
                code2 = QlCode.build();
                space = true;
            } else {
                Token t = current();
                if (t.isSpecial()) {
                    space = false;
                } else if (space) {
                    code.append(" ");
                } else {
                    space = true;
                }
                code.append(t.code);
            }
        }
        return code;
    }
    
    protected Attribute attribute() {
        int start = cIndex;
        Expression e = expression();
        if (e == null) return null;
        Token t = current();
        QlCode alias = null;
        String key;
        if (t.isWord("AS")) {
            t = next();
            if (!t.isIdentifier() && !t.isWord()) {
                expected("Alias");
                setIndex(start);
                return null;
            }
            key = t.value;
            alias = t.code;
            next();
        } else if (t.isIdentifier() || t.isPlainWord()) {
            key = t.value;
            alias = t.code;
            next();
        } else if (!e.getColumnRefs().isEmpty()) {
            ObjectRef c = e.columnRefs.get(e.columnRefs.size()-1);
            key = c.getKey();
        } else {
            expected("Alias");
            setIndex(start);
            return null;
        }
        return new Attribute(key, e.getColumnRefs(), e.expression, alias);
    }
    
    protected List<Attribute> attributes() {
        List<Attribute> result = new ArrayList<>();
        Attribute a = attribute();
        if (a == null) return null;
        result.add(a);
        while (current().isSpecial(",")) {
            int i = cIndex;
            next();
            a = attribute();
            if (a == null) {
                setIndex(i);
                expected("Attribute");
                return result;
            }
            result.add(a);
        }
        return result;
    }
    
    protected Table table() {
        int start = cIndex;
        ObjectRef o = objectRef();
        Expression e = o == null ? expressionValue() : null;
        if (o == null && e == null) return null;
        Token t = current();
        QlCode alias = null;
        String key;
        if (t.isIdentifier() || t.isPlainWord()) {
            key = t.value;
            alias = t.code;
            next();
        } else if (o != null) {
            key = o.getKey();
//        } else if (e != null && !e.getColumnRefs().isEmpty()) {
//            ObjectRef c = e.columnRefs.get(e.columnRefs.size()-1);
//            key = c.getKey();
        } else {
            expected("Alias");
            setIndex(start);
            return null;
        }
        if (o != null) {
            return new Table(key, Collections.emptyList(), o.getExpression(), alias);
        } else {
            assert e != null;
            return new Table(key, e.columnRefs, e.expression, alias);
        }
    }
    
    protected JoinPart join(SqlJoinableClause.JoinType type) {
        int start = cIndex;
        Table table = table();
        if (table == null) return null;
        if (!readKeyword("ON")) return new JoinPart(type, table, null);
        Expression e = expression();
        if (e == null) {
            setIndex(start);
            return null;
        }
        return new JoinPart(type, table, e);
    }
    
    protected SelectPart selectPart(boolean requireKeyword) {
        int start = cIndex;
        if (!readKeyword("SELECT") && requireKeyword) {
            expected("SELECT");
            return null;
        }
        List<Attribute> attributes = attributes();
        if (attributes == null) {
            setIndex(start);
            return null;
        }
        return new SelectPart(attributes);
    }
    
    protected TablePart fromPart(boolean requireKeyword) {
        int start = cIndex;
        if (!readKeyword("FROM") && requireKeyword) {
            expected("FROM");
            return null;
        }
        Table table = table();
        if (table == null) {
            setIndex(start);
            return null;
        }
        return new TablePart(table);
    }
    
    protected JoinPart joinPart(boolean requireKeyword) {
        int start = cIndex;
        SqlJoinableClause.JoinType type = SqlJoinableClause.JoinType.INNER;
        if (!readKeyword("INNER")) {
            if (readKeyword("LEFT")) {
                readKeyword("OUTER");
                type = SqlJoinableClause.JoinType.LEFT;
            } else if (readKeyword("RIGHT")) {
                readKeyword("OUTER");
                type = SqlJoinableClause.JoinType.RIGHT;
            } else if (readKeyword("FULL")) {
                readKeyword("OUTER");
                type = SqlJoinableClause.JoinType.OUTER;
            } else if (readKeyword("OUTER")) {
                type = SqlJoinableClause.JoinType.OUTER;
            }
        }
        if (!readKeyword("JOIN") && requireKeyword) {
            setIndex(start);
            return null;
        }
        return join(type);
    }
    
    protected WherePart wherePart(boolean requireKeyword) {
        int start = cIndex;
        if (!readKeyword("WHERE") && requireKeyword) {
            expected("WHERE");
            return null;
        }
        Expression e = expression();
        if (e == null) {
            setIndex(start);
            return null;
        }
        return new WherePart(e);
    }
    
    protected GroupByPart groupByPart(boolean requireKeyword) {
        int start = cIndex;
        if (readKeyword("GROUP")) {
            if (!readKeyword("BY")) {
                expected("BY");
                setIndex(start);
                return null;
            }
        } else if (requireKeyword) {
            expected("GROUP BY");
            return null;
        }
        Expression e = expressionList();
        if (e == null) {
            setIndex(start);
            return null;
        }
        return new GroupByPart(e);
    }
    
    protected HavingPart havingPart(boolean requireKeyword) {
        int start = cIndex;
        if (!readKeyword("HAVING") && requireKeyword) {
            expected("HAVING");
            return null;
        }
        Expression e = expression();
        if (e == null) {
            setIndex(start);
            return null;
        }
        return new HavingPart(e);
    }
    
    protected OrderByPart orderByPart(boolean requireKeyword) {
        int start = cIndex;
        if (readKeyword("ORDER")) {
            if (!readKeyword("BY")) {
                expected("BY");
                setIndex(start);
                return null;
            }
        } else if (requireKeyword) {
            expected("ORDER BY");
            return null;
        }
        Expression e = expressionList();
        if (e == null) {
            setIndex(start);
            return null;
        }
        return new OrderByPart(e);
    }
    
    protected boolean selectStmtPart(SelectStmt stmt, String part, boolean requireKeyword) {
        if (part == null) return false;
        switch (part) {
            case "SELECT":
                SelectPart sp = selectPart(requireKeyword);
                if (sp == null) return false;
                stmt.selectParts.add(sp);
                return true;
            case "FROM":
                TablePart tp = fromPart(requireKeyword);
                if (tp == null) return false;
                stmt.tableParts.add(tp);
                return true;
            case "INNER":
            case "LEFT":
            case "RIGHT":
            case "FULL":
            case "OUTER":
            case "JOIN":
                JoinPart jp = joinPart(requireKeyword);
                if (jp == null) return false;
                stmt.joinParts.add(jp);
                return true;
            case "WHERE":
                WherePart wp = wherePart(requireKeyword);
                if (wp == null) return false;
                stmt.whereParts.add(wp);
                return true;
            case "GROUP":
                GroupByPart gp = groupByPart(requireKeyword);
                if (gp == null) return false;
                stmt.groupByParts.add(gp);
                return true;
            case "HAVING":
                HavingPart hp = havingPart(requireKeyword);
                if (hp == null) return false;
                stmt.havingParts.add(hp);
                return true;
            case "ORDER":
                OrderByPart op = orderByPart(requireKeyword);
                if (op == null) return false;
                stmt.orderByParts.add(op);
                return true;
            default:
                return false;
        }
    }
    
    protected SelectStmt partialSelectStmt(String defaultPart) {
        SelectStmt stmt = new SelectStmt();
        boolean first = true;
        while (!atEnd()) {
            Token c = current();
            if (!selectStmtPart(stmt, c.value, true)) {
                break;
            }
            first = false;
        }
        if (first && !selectStmtPart(stmt, defaultPart, false)) {
            return null;
        }
        return stmt;
    }
    
    public <T> T parse(MiFunction<MiSqlParser, T> action) {
        T t = action.apply(this);
        requireAtEnd();
        return t;
    }
    
    public QlCode parseCode() {
        return parse(MiSqlParser::code);
    }
    
    public Attribute parseAttribute() {
        return parse(MiSqlParser::attribute);
    }
    
    public List<Attribute> parseAttributes() {
        return parse(MiSqlParser::attributes);
    }
    
    public SelectPart parseSelectPart() {
        return parse(p -> p.selectPart(false));
    }
    
    public TablePart parseFromPart() {
        return parse(p -> p.fromPart(false));
    }
    
    public JoinPart parseJoinPart() {
        return parse(p -> p.joinPart(false));
    }
    
    public SelectStmt parsePartialSelectStmt(String defaultPart) {
        return parse(p -> p.partialSelectStmt(defaultPart));
    }
    
    public void parsePartialSelectStmtOrCode(String defaultPart, SelectBuilder stmtBuilder, QlBuilder<?> qlBuilder) {
        SelectStmt stmt = partialSelectStmt(defaultPart);
        if (stmt != null && atEnd()) {
            stmt.appendTo(stmtBuilder);
            return;
        }
        setIndex(0);
        QlCode c = parseCode();
        c.appendTo(qlBuilder);
    }
    
    private static final String[] KEYWORDS = {
        "SELECT", "AS", "FROM", 
        "INSERT", "INTO", "UPDATE", "TABLE", "DELETE",
        "INNER", "LEFT", "RIGHT", "FULL", "OUTER", "JOIN", "ON", 
        "WHERE", "GROUP", "HAVING", "ORDER", 
        "AND", "OR", "UNION"};
    static { Arrays.sort(KEYWORDS); }
    
    public static boolean isKeyword(String word) {
        return Arrays.binarySearch(KEYWORDS, word.toUpperCase()) >= 0;
    }
    
    protected static final Token T_ERROR = new Token("<Error>", QlCode.ql("<Error>"), TokenType.ERROR);
    protected static final Token T_AT_END = new Token("<EOI>", QlCode.ql("<EOI>"), TokenType.AT_END);
    
    protected static class Token {
        public final String value;
        public final QlCode code;
        public final TokenType type;

        public Token(String value, QlCode code, TokenType type) {
            this.value = value;
            this.code = code;
            this.type = type;
        }
        
        public boolean isEnd() {
            return type == TokenType.AT_END;
        }
        
        public boolean isIdentifier() {
            return type == TokenType.IDENTIFIER;
        }

        public boolean isOperator() {
            return type == TokenType.OPERATOR;
        }
        
        public boolean isSpecial() {
            return type == TokenType.SPECIAL;
        }

        public boolean isSpecial(String value) {
            return type == TokenType.SPECIAL && this.value.equals(value);
        }

        public boolean isValue() {
            return type == TokenType.VALUE;
        }
        
        public boolean isWord() {
            return type == TokenType.WORD;
        }
        
        public boolean isWordOrIdentifier() {
            return isWord() || isIdentifier();
        }

        public boolean isWord(String value) {
            return type == TokenType.WORD && this.value.equalsIgnoreCase(value);
        }
        
        public boolean isKeyword() {
            return isWord() && MiSqlParser.isKeyword(value);
        }

        public boolean isPlainWord() {
            return isWord() && !MiSqlParser.isKeyword(value);
        }

        @Override
        public String toString() {
            return type + " " + code;
        }
    }
    
    protected static enum TokenType {
        VALUE,
        WORD,
        IDENTIFIER,
        SPECIAL,
        OPERATOR,
        AT_END,
        ERROR;
    }
    
    public static class Expression implements QlCode {
        protected final List<ObjectRef> columnRefs;
        protected final QlCode expression;

        public Expression(QlCode expression) {
            this.columnRefs = new ArrayList<>(3);
            this.expression = expression;
        }

        public Expression(List<ObjectRef> columnRefs, QlCode expression) {
            this.columnRefs = columnRefs;
            this.expression = expression;
        }

        public List<ObjectRef> getColumnRefs() {
            return columnRefs;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            expression.appendTo(qlBuilder);
        }

        @Override
        public String toString() {
            return expression.toString();
        }
    }
    
    public static class ObjectRef {
        private final String parentKey;
        private final String key;
        private final QlCode expression;

        @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
        public ObjectRef(String parentKey, String key, QlCode expression) {
            this.parentKey = parentKey;
            this.key = key;
            this.expression = expression;
        }

        public String getKey() {
            return key;
        }

        public String getParentKey() {
            return parentKey;
        }

        public QlCode getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return (parentKey != null ? parentKey + "." : "") + key;
        }
    }
    
    public static abstract class Declaration extends Expression {
        private final String key;
        private final QlCode alias;

        public Declaration(String key, List<ObjectRef> columnRefs, QlCode expression, QlCode alias) {
            super(columnRefs, expression);
            this.key = key;
            this.alias = alias;
        }

        public String getKey() {
            return key;
        }

        public QlCode getExpression() {
            return expression;
        }

        public QlCode getAlias() {
            return alias;
        }

        @Override
        public String toString() {
            return expression + (alias == null ? "" : ": " + alias);
        }
    }
    
    public static class Attribute extends Declaration {

        public Attribute(String key, List<ObjectRef> columnRefs, QlCode expression, QlCode alias) {
            super(key, columnRefs, expression, alias);
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            getExpression().appendTo(qlBuilder);
            if (getAlias() != null) {
                qlBuilder.ql(" AS ").ql(getAlias());
            }
        }
    }
    
    public static class Table extends Declaration {

        public Table(String key, List<ObjectRef> columnRefs, QlCode expression, QlCode alias) {
            super(key, columnRefs, expression, alias);
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            getExpression().appendTo(qlBuilder);
            if (getAlias() != null) {
                qlBuilder.ql(" ").ql(getAlias());
            }
        }
    }
    
    public static class SelectPart implements SqlBuilder.Code<SelectBuilder> {

        private final List<Attribute> attributes;

        public SelectPart(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }
        
        @Override
        public void appendTo(SelectBuilder c) {
            SelectBuilder.Select select = c.select();
            attributes.forEach(a -> a.appendTo(select.and()));
        }

        @Override
        public String toString() {
            return "SELECT " + attributes;
        }
    }
    
    public static class TablePart implements SqlBuilder.Code<SqlTableClause> {
        
        private final Table table;

        public TablePart(Table table) {
            this.table = table;
        }

        public Table getTable() {
            return table;
        }

        @Override
        public void appendTo(SqlTableClause c) {
            table.appendTo(c.table());
        }

        @Override
        public String toString() {
            return "FROM " + table;
        }
    }
    
    public static class JoinPart implements SqlBuilder.Code<SqlJoinableClause> {
        private final SqlJoinableClause.JoinType type;
        private final Table table;
        private final Expression condition;

        public JoinPart(SqlJoinableClause.JoinType type, Table table, Expression condition) {
            this.type = type;
            this.table = table;
            this.condition = condition;
        }

        public SqlJoinableClause.JoinType getType() {
            return type;
        }

        public Table getTable() {
            return table;
        }

        public Expression getCondition() {
            return condition;
        }

        @Override
        public void appendTo(SqlJoinableClause c) {
            SqlJoinableClause.Join<?> j = c.join(getType());
            getTable().appendTo(j);
            Expression on = getCondition();
            if (on != null) on.appendTo(j.on());
        }
    }
    
    public static abstract class ExpressionPart<B> implements SqlBuilder.Code<B> {
        private final Expression expression;

        public ExpressionPart(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }
    }
    
    public static class WherePart extends ExpressionPart<SqlFilterableClause> {

        public WherePart(Expression expression) {
            super(expression);
        }

        @Override
        public void appendTo(SqlFilterableClause c) {
            getExpression().appendTo(c.where());
        }
    }
    
    public static class GroupByPart extends ExpressionPart<SelectBuilder> {

        public GroupByPart(Expression expression) {
            super(expression);
        }
        
        @Override
        public void appendTo(SelectBuilder c) {
            getExpression().appendTo(c.groupBy());
        }
    }
    
    public static class HavingPart extends ExpressionPart<SelectBuilder> {

        public HavingPart(Expression expression) {
            super(expression);
        }
       
        @Override
        public void appendTo(SelectBuilder c) {
            getExpression().appendTo(c.having());
        }
    }
    
    public static class OrderByPart extends ExpressionPart<SelectBuilder> {

        public OrderByPart(Expression expression) {
            super(expression);
        }

        @Override
        public void appendTo(SelectBuilder c) {
            getExpression().appendTo(c.groupBy());
        }
    }
    
    public static class SelectStmt implements SqlBuilder.Code<SelectBuilder> {
        private final List<SelectPart> selectParts = new ArrayList<>(3);
        private final List<TablePart> tableParts = new ArrayList<>(1);
        private final List<JoinPart> joinParts = new ArrayList<>(3);
        private final List<WherePart> whereParts = new ArrayList<>(1);
        private final List<GroupByPart> groupByParts = new ArrayList<>(1);
        private final List<HavingPart> havingParts = new ArrayList<>(1);
        private final List<OrderByPart> orderByParts = new ArrayList<>(1);

        public List<SelectPart> getSelectParts() {
            return selectParts;
        }

        public List<TablePart> getTableParts() {
            return tableParts;
        }

        public List<JoinPart> getJoinParts() {
            return joinParts;
        }

        public List<WherePart> getWhereParts() {
            return whereParts;
        }

        public List<GroupByPart> getGroupByParts() {
            return groupByParts;
        }

        public List<HavingPart> getHavingParts() {
            return havingParts;
        }

        public List<OrderByPart> getOrderByParts() {
            return orderByParts;
        }

        @Override
        public void appendTo(SelectBuilder c) {
            selectParts.forEach(p -> p.appendTo(c));
            tableParts.forEach(p -> p.appendTo(c));
            joinParts.forEach(p -> p.appendTo(c));
            whereParts.forEach(p -> p.appendTo(c));
            groupByParts.forEach(p -> p.appendTo(c));
            havingParts.forEach(p -> p.appendTo(c));
            orderByParts.forEach(p -> p.appendTo(c));
        }
    }
    
//    private static class Ident implements QlCode {
//        private final String name;
//        private final boolean quoted;
//
//        public Ident(String name, boolean quoted) {
//            this.name = name;
//            this.quoted = quoted;
//        }
//
//        public String getName() {
//            return name;
//        }
//        
//        @Override
//        public void appendTo(QlBuilder<?> qlBuilder) {
//            if (quoted) {
//                qlBuilder.id(name);
//            } else {
//                qlBuilder.ql(getName());
//            }
//        }
//    }
//    
////    public static String[][] parseSelectClause(String select) {
////        List<String> parts = splitSelectClause(select);
////        final String[][] result = new String[parts.size()][];
////        for (int i = 0; i < result.length; i++) {
////            result[i] = parseSelectPart(parts.get(i));
////        }
////        return result;
////    }
////    
//    public static List<Attribute> parseAttributes(String select) {
//        List<String> parts = splitSelectClause(select);
//        final List<Attribute> result = new ArrayList<>();
//        for (int i = 0; i < parts.size(); i++) {
//            result.add(parseAttributePart(parts.get(i)));
//        }
//        return result;
//    }
//    
//    protected static List<String> splitSelectClause(String select) {
//        List<String> parts = new ArrayList<>();
//        for (int i = 0; i < select.length(); i++) {
//            int start = i;
//            i = scanUntil(i, select, ',');
//            parts.add(select.substring(start, i).trim());
//        }
//        return parts;
//    }
////    
////    
////    private static final Pattern SELECT_PART_PATTERN = PartPattern(
////            "(((?:(IDENT)_\\._)?(IDENT))(?:_AS_(IDENT))?)"
////             + "|" +
////             "((.+?)AS_(IDENT))");
////    
////    public static String[] parseSelectPart(String select) {
////        select = select.trim();
////        Matcher m = SELECT_PART_PATTERN.matcher(select);
////        if (!m.matches()) {
////            throw new IllegalArgumentException("Cannot parse: " + select);
////        }
////        if (!m.matches()) {
////            throw new IllegalArgumentException("Cannot parse: " + select);
////        }
////        final String key, def, table, column;
////        if (m.group(1) != null) {
////            def = m.group(2);
////            table = m.group(3);
////            column = m.group(4);
////            key = m.group(5) != null ? m.group(5) : m.group(4);
////        } else {
////            def = m.group(7).trim();
////            key = m.group(8);
////            table = null;
////            column = null;
////        }
////        return new String[]{stripQuotes(key), def, stripQuotes(table), 
////            stripQuotes(column), key, column};
////    }
////    
//    private static final Pattern ATTRIBUTE_PART_PATTERN = PartPattern(
//            "((?:(IDENT)_\\._)?(IDENT))(?:_(?:AS_)?(IDENT))?");
//    
//    public static Attribute parseAttributePart(String attribute) {
//        attribute = attribute.trim();
//        Matcher m = ATTRIBUTE_PART_PATTERN.matcher(attribute);
//        if (!m.matches()) {
//            return parseAttributeExPart(attribute);
//        }
//        final String key, table;
//        QlCode expression, alias;
//        expression = parseExpression(m.group(1));
//        table = stripQuotes(m.group(2));
//        if (m.group(4) != null) {
//            key = stripQuotes(m.group(4));
//            alias = parseExpression(m.group(4));
//        } else {
//            key = stripQuotes(m.group(3));
//            alias = null;
//        }
//        return new Attribute(key, table, expression, alias);
//    }
//    
//    private static final Pattern ATTRIBUTE_EX_PART_PATTERN = PartPattern(
//            "(.+?)(?:AS_)?(IDENT)");
//    
//    private static Attribute parseAttributeExPart(String attribute) {
//        Matcher m = ATTRIBUTE_EX_PART_PATTERN.matcher(attribute);
//        if (!m.matches()) {
//            throw new IllegalArgumentException("Cannot parse: " + attribute);
//        }
//        final String key;
//        QlCode expression, alias;
//        expression = parseExpression(m.group(1));
//        alias = parseExpression(m.group(2));
//        key = stripQuotes(m.group(2));
//        return new Attribute(key, null, expression, alias);
//    }
//    
//    private static final Pattern FROM_PART_PATTERN = PartPattern(
//            "(?:(.*)_)?(IDENT)");
//    
//    public static Table parseFromPart(String schema, String from) {
//        Matcher m = FROM_PART_PATTERN.matcher(from.trim());
//        if (!m.matches()) {
//            throw new IllegalArgumentException("Cannot parse: " + from);
//        }
//        final String key;
//        QlCode table, name;
//        key = stripQuotes(m.group(2));
//        if (m.group(1) == null) {
//            table = parseExpression(m.group(2));
//            if (!schema.isEmpty()) {
//                table = QlCode.id(schema).ql(table);
//            }
//            name = null;
//        } else {
//            table = parseExpression(m.group(1));
//            name = parseExpression(m.group(2));
//        }
//        return new Table(key, table, name);
//    }
//    
//    private static final Pattern JOIN_PART_PATTERN = PartPattern(
//            "(RIGHT(?:~OUTER)?|LEFT(?:~OUTER)?|INNER|OUTER)?(?:_JOIN)?(.*)?(IDENT)(?:_ON_(.*))?");
//    
//    public static JoinTable parseJoinPart(String join) {
//        Matcher m = JOIN_PART_PATTERN.matcher(join.trim());
//        if (!m.matches()) {
//            throw new IllegalArgumentException("Cannot parse: " + join);
//        }
//        final String key, joinType;
//        final QlCode declaration, onClause;
//        if (m.group(1) != null) {
//            joinType = m.group(1);
//        } else {
//            joinType = "INNER";
//        }
//        if (m.group(2) != null) {
//            declaration = parseExpression(m.group(2)).ql(" ").ql(parseExpression(m.group(3)));
//        } else {
//            declaration = parseExpression(m.group(3));
//        }
//        key = stripQuotes(m.group(3));
//        if (m.group(4) != null) {
//            onClause = parseExpression(m.group(4));
//        } else {
//            onClause = null;
//        }
//        return new JoinTable(key, joinType, declaration, onClause);
//    }
//
////    private static final Pattern GROUP_PART_PATTERN = PartPattern(
////            "(IDENT_\\._)?(IDENT)");
////    
////    public static String[] parseGroupPart(String group) {
////        Matcher m = GROUP_PART_PATTERN.matcher(group.trim());
////        if (!m.matches()) {
////            throw new IllegalArgumentException("Cannot parse: " + group);
////        }
////        final String key;
////        if (m.group(2) != null) {
////            key = m.group(2);
////        } else {
////            throw new AssertionError(m.toString());
////        }
////        return new String[]{stripQuotes(key), group};
////    }
////    
////    private static final Pattern ORDER_PART_PATTERN = PartPattern(
////            "((IDENT)_\\._)?(IDENT)(_ASC|_DESC)?");
////    
////    public static String[] parseOrderPart(String orderBy) {
////        Matcher m = ORDER_PART_PATTERN.matcher(orderBy.trim());
////        if (!m.matches()) {
////            throw new IllegalArgumentException("Cannot parse: " + orderBy);
////        }
////        final String key, required;
////        if (m.group(3) != null) {
////            key = m.group(3);
////        } else {
////            throw new AssertionError(m.toString());
////        }
////        if (m.group(2) != null) {
////            required = stripQuotes(m.group(2)) + "." + stripQuotes(key);
////        } else {
////            required = null;
////        }
////        return new String[]{stripQuotes(key), orderBy, stripQuotes(required)};
////    }
////    
//    
//    private static final Pattern QUICK_FILTER_PATTERN = PartPattern(
//            "_(IDENT)_([=!<>~%]|LIKE|like)_");
//    
//    public static QuickFilter tryParseQuickFilter(String sql) {
//        Matcher m = QUICK_FILTER_PATTERN.matcher(sql);
//        if (!m.matches()) return null;
//        return new QuickFilter(m.group(1), m.group(2));
//    }
//    
//    private static final Pattern SPECIAL_PATTERN = Pattern.compile(
//            "(" + QuotedStringPattern("`") + ")|(" +
//                QuotedStringPattern("'") + "|" + QuotedStringPattern("\"") +
//                    ")");
//    
//    public static QlCode.Builder parseExpression(String sql) {
//        QlCode.Builder result = QlCode.build();
//        Matcher m = SPECIAL_PATTERN.matcher(sql);
//        int n = 0;
//        while (m.find()) {
//            if (m.start() > n) {
//                result.append(sql.subSequence(n, m.start()));
//            }
//            if (m.group(1) != null) {
//                result.identifier(stripQuotes(m.group(1)));
//            } else {
//                result.stringLiteral(stripQuotes(m.group(2)));
//            }
//            n = m.end();
//        }
//        if (n < sql.length()) {
//            result.append(sql.subSequence(n, sql.length()));
//        }
//        return result;
//    }
//    
//    private static final char[] BR_OPEN =  {'(', '[', '{'};
//    private static final char[] BR_CLOSE = {')', ']', '}'};
//    private static final char[] QUOTE = {'\'', '"', '`'};
//    private static final char ESCAPE = '\\';
//    
//    private static int scanUntil(int i, String select, char end) {
//        for (;i < select.length(); i++) {
//            char c = select.charAt(i);
//            if (c == end) {
//                return i;
//            } else {
//                for (int br = 0; br < BR_OPEN.length; br++) {
//                    if (c == BR_OPEN[br]) {
//                        i = scanUntil(i+1, select, BR_CLOSE[br]);
//                    }
//                }
//                for (char q: QUOTE) {
//                    if (c == q) {
//                        i = scanQuote(i+1, select, q);
//                        break;
//                    }
//                }
//            }
//        }
//        return i;
//    }
//    
//    private static int scanQuote(int i, String select, char q) {
//        for (;i < select.length(); i++) {
//            char c = select.charAt(i);
//            if (c == q) {
//                return i;
//            } else if (c == ESCAPE) {
//                i++;
//            }
//        }
//        return i;
//    }
//    
//    private static String stripQuotes(String s) {
//        if (s == null || s.isEmpty()) return s;
//        char c0 = s.charAt(0);
//        for (char q: QUOTE) {
//            if (c0 == q) {
//                if (s.length() < 2 || s.charAt(s.length()-1) != q) {
//                    throw new IllegalArgumentException("Illegal quotes: " + s);
//                }
//                return s.substring(1, s.length()-1);
//            }
//        }
//        return s;
//    }
//    
//    public static class Attribute implements QlCode {
//        private final String key;
//        private final String tableKey;
//        private final QlCode expression;
//        private final QlCode alias;
//
//        public Attribute(String key, String tableKey, QlCode expression, QlCode alias) {
//            this.key = key;
//            this.tableKey = tableKey;
//            this.expression = expression;
//            this.alias = alias;
//        }
//
//        @Override
//        public void appendTo(QlBuilder<?> qlBuilder) {
//            qlBuilder.append(expression);
//            if (alias != null) {
//                qlBuilder.append(" AS ").append(alias);
//            }
//        }
//
//        public String getKey() {
//            return key;
//        }
//
//        public String getTableKey() {
//            return tableKey;
//        }
//
//        public QlCode getExpression() {
//            return expression;
//        }
//
//        public QlCode getAlias() {
//            return alias;
//        }
//    }
//    
//    public static class Table implements QlCode {
//        private final String key;
//        private final QlCode declaration;
//        private final QlCode name;
//
//        public Table(String key, QlCode declaration, QlCode name) {
//            this.key = key;
//            this.declaration = declaration;
//            this.name = name;
//        }
//
//        public String getKey() {
//            return key;
//        }
//
//        @Override
//        public void appendTo(QlBuilder<?> qlBuilder) {
//            declaration.appendTo(qlBuilder);
//            if (name != null) {
//                qlBuilder.ql(" ").ql(name);
//            }
//        }
//    }
//    
//    public static class JoinTable {
//        private final String key;
//        private final String joinType;
//        private final QlCode declaration;
//        private final QlCode onClause;
//
//        public JoinTable(String key, String joinType, QlCode declaration, QlCode onClause) {
//            this.key = key;
//            this.joinType = joinType;
//            this.declaration = declaration;
//            this.onClause = onClause;
//        }
//
//        public String getKey() {
//            return key;
//        }
//
//        public String getJoinType() {
//            return joinType;
//        }
//
//        public QlCode getDeclaration() {
//            return declaration;
//        }
//
//        public QlCode getOnClause() {
//            return onClause;
//        }
//    }
//    
//    public static class QuickFilter {
//        
//        private final String key;
//        private final String operation;
//
//        public QuickFilter(String key, String operation) {
//            this.key = key;
//            this.operation = operation;
//        }
//
//        public String getKey() {
//            return key;
//        }
//
//        public String getOperation() {
//            return operation;
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 31 * hash + Objects.hashCode(this.key);
//            hash = 31 * hash + Objects.hashCode(this.operation);
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null || getClass() != obj.getClass()) {
//                return false;
//            }
//            final QuickFilter other = (QuickFilter) obj;
//            return Objects.equals(this.key, other.key)
//                    && Objects.equals(this.operation, other.operation);
//        }
//    }
}
