package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SelectableQueryPart;
import org.cthul.miro.query.parts.ValuesQueryPart;
import static org.cthul.miro.query.sql.DataQuery.Type.*;

public interface DataQueryPart<Builder> extends QueryPartType<Builder> {
    
    public static final DataQueryPart<SelectQueryBuilder> SELECT =  Type.SELECT;
    public static final DataQueryPart<QueryAdapter<?>>    TABLE =   Type.TABLE;
    public static final DataQueryPart<QueryAdapter<?>>    JOIN =    Type.JOIN;
    public static final DataQueryPart<UpdateQueryBuilder> SET =     Type.SET;
    public static final DataQueryPart<QueryAdapter<?>>    VALUES =  Type.VALUES;
    public static final DataQueryPart<InsertQueryBuilder> SUBQUERY = Type.SUBQUERY;
    public static final DataQueryPart<QueryAdapter<?>>    WHERE =   Type.WHERE;
    public static final DataQueryPart<SelectQueryBuilder> GROUP_BY = Type.GROUP_BY;
    public static final DataQueryPart<SelectQueryBuilder> HAVING =  Type.HAVING;
    public static final DataQueryPart<SelectQueryBuilder> ORDER_BY = Type.ORDER_BY;
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum Type implements DataQueryPart {

        SELECT {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asSelect(query).select(part);
            }
        },
        TABLE {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                switch (DataQuery.Type.get(query)) {
                    case SELECT:
                        asSelect(query).from(part);
                        return;
                    case INSERT:
                        asInsert(query).into(part);
                        return;
                    case UPDATE:
                        asUpdate(query).update(part);
                        return;
                }
                throw unexpectedQueryType(query.getQueryType());
            }
        },
        JOIN {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                switch (DataQuery.Type.get(query)) {
                    case SELECT:
                        asSelect(query).join(part);
                        return;
//                    case INSERT:
//                        cast(InsertQueryBuilder.class, queryBuilder).join(part);
//                        return;
                    case UPDATE:
                        asUpdate(query).join(part);
                        return;
                }
                throw unexpectedQueryType(query.getQueryType());
            }
        },
        SET {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asUpdate(query).set(part);
            }
        },
        VALUES {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                switch (DataQuery.Type.get(query)) {
                    case INSERT:
                        asInsert(query).values(asSelectable(part));
                        return;
                    case UPDATE:
                        asUpdate(query).values(asValues(part));
                        return;
                }
                throw unexpectedQueryType(query.getQueryType());
            }
        },
        SUBQUERY {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asInsert(query).from(asSelectable(part));
            }
        },
        WHERE {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                switch (DataQuery.Type.get(query)) {
                    case SELECT:
                        asSelect(query).where(part);
                        return;
                    case UPDATE:
                        asUpdate(query).where(part);
                        return;
                }
                throw unexpectedQueryType(query.getQueryType());
            }
        },
        GROUP_BY {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asSelect(query).groupBy(part);
            }
        },
        HAVING {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asSelect(query).having(part);
            }
        },
        ORDER_BY {
            @Override
            public void addPartTo(QueryPart part, QueryAdapter query) {
                asSelect(query).orderBy(part);
            }
        };
        
        protected <T> T cast(Class<T> clazz, Object o) {
            if (clazz.isInstance(o)) {
                return clazz.cast(o);
            }
            throw new IllegalArgumentException(
                    "Expected " + clazz + ", got " + o);
        }
        
        protected RuntimeException unexpectedQueryType(QueryType<?> type) {
            throw new IllegalArgumentException(
                    "Unexpected query type " + type);
        }
        
        protected SelectQueryBuilder asSelect(QueryAdapter<?> query) {
            return cast(SelectQueryBuilder.class, query.getBuilder());
        }
        
        protected InsertQueryBuilder asInsert(QueryAdapter<?> query) {
            return cast(InsertQueryBuilder.class, query.getBuilder());
        }
        
        protected UpdateQueryBuilder asUpdate(QueryAdapter<?> query) {
            return cast(UpdateQueryBuilder.class, query.getBuilder());
        }
        
        protected SelectableQueryPart asSelectable(QueryPart part) {
            return cast(SelectableQueryPart.class, part);
        }
        
        protected ValuesQueryPart asValues(QueryPart part) {
            return cast(ValuesQueryPart.class, part);
        }
        
//        protected SelectQueryBuilder asDelete(QueryAdapter<?> query) {
//            return cast(SelectQueryBuilder.class, query);
//        }
    }
}
