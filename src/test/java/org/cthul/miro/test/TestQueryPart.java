package org.cthul.miro.test;


import java.util.Arrays;
import java.util.List;
import org.cthul.miro.query.api.AttributeQueryPart;
import org.cthul.miro.query.api.QueryPart;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.api.SelectableQueryPart;
import org.cthul.miro.query.sql.DataQueryPartType;

public class TestQueryPart implements QueryPart {
    
    protected final QueryPartType type;
    protected final String sql;
    protected final Object[] args;

    public TestQueryPart(QueryPartType type, String sql, Object... args) {
        this.type = type;
        this.sql = sql;
        this.args = args;
    }
    
    @Override
    public QueryPartType getPartType() {
        return type;
    }

    @Override
    public void put(String key, Object[] args) {
    }

    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        sqlBuilder.append(sql);
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        args.addAll(Arrays.asList(this.args));
    }

    @Override
    public String toString() {
        return type + " " + sql + " " + Arrays.toString(args);
    }
    
    public static class Attribute extends TestQueryPart implements AttributeQueryPart {
        
        public Attribute(String attribute) {
            super(DataQueryPartType.ATTRIBUTE, attribute);
        }

        @Override
        public String getAttribute() {
            return sql;
        }
    }
    
    private static String tuple(int l) {
        StringBuilder sb = new StringBuilder(2+2*l);
        sb.append('(');
        for (int i = 0; i < l; i++) {
            if (i != 0) sb.append(',');
            sb.append('?');
        }
        return sb.append(')').toString();
    }
    
    public static class Values extends TestQueryPart implements SelectableQueryPart {

        public Values(Object... args) {
            super(DataQueryPartType.VALUES, tuple(args.length), args);
        }

        @Override
        public void selectAttribute(String attribute) {
        }
    }
}
