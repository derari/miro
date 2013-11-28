package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.api.QueryType;

public interface DataQuery<Builder extends QueryBuilder<? extends Builder>> extends QueryType<Builder> {

    public static final DataQuery<SelectBuilder<?>> SELECT = Type.SELECT;
    public static final DataQuery<InsertBuilder<?>> INSERT = Type.INSERT;
    public static final DataQuery<UpdateBuilder<?>> UPDATE = Type.UPDATE;
    public static final DataQuery<UpdateBuilder<?>> DELETE = Type.DELETE;
    public static final DataQuery<?>  UNION  = Type.UNION;
    
    QueryString<Builder> newQueryString(QuerySyntax syntax);
    
    JdbcQuery<Builder> newJdbcQuery(JdbcAdapter jdbcAdapter);
    
    Builder getBuilder(QueryAdapter<?> adapter);
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum Type implements DataQuery {
        
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        UNION,
        UNKNOWN;

        @Override
        public QueryString newQueryString(QuerySyntax syntax) {
            return syntax.newQueryString(this);
        }

        @Override
        public JdbcQuery newJdbcQuery(JdbcAdapter jdbcAdapter) {
            return jdbcAdapter.newJdbcQuery(this);
        }

        @Override
        public QueryBuilder getBuilder(QueryAdapter adapter) {
            if (adapter.getQueryType() != this) {
                throw new IllegalArgumentException(
                        adapter + " is not a " + this + " query");
            }
            return adapter.getBuilder();
        }
        
        public static Type get(QueryAdapter<?> query) {
            return get(query.getQueryType());
        }
        
        public static Type get(QueryType<?> type) {
            if (type instanceof Type) {
                return (Type) type;
            }
            return Type.UNKNOWN;
        }
    }
}
