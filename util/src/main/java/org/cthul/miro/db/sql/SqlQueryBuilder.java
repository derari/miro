package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.syntax.RequestBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.cthul.miro.db.*;
import org.cthul.miro.db.syntax.RequestBuilderDelegator;
import org.cthul.miro.db.syntax.RequestString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class SqlQueryBuilder implements SqlQuery {
    
    private final MiConnection connection;
    private final Supplier<RequestString> queryDialect;
    private final SelectBuilder select;
    private final FromBuilder from;
    private final List<JoinBuilder> joins = new ArrayList<>();
    private WhereBuilder where = null;
    private GroupByBuilder groupBy = null;
    private HavingBuilder having = null;
    private OrderByBuilder orderBy = null;

    public SqlQueryBuilder(MiConnection connection, Supplier<RequestString> queryDialect) {
        this.connection = connection;
        this.queryDialect = queryDialect;
        select = new SelectBuilder();
        from = new FromBuilder();
    }
    
    protected MiQueryString query() {
        return query(connection.newQuery());
    }
    
    protected <Q extends RequestBuilder<?>> Q query(Q query) {
        append(query, "SELECT ", select, true);
        append(query, " FROM ", from, true);
        joins.forEach(j -> {
            append(query, j);
        });
        append(query, " WHERE ", where);
        append(query, " GROUP BY ", groupBy);
        append(query, " HAVING ", having);
        return query;
    }
    
    private void append(RequestBuilder<?> target, ClauseBuilder<?> part) {
        target.append(part.toString());
        target.pushArguments(part.getArguments());
    }
    
    private void append(RequestBuilder<?> target, String prefix, ClauseBuilder<?> part) {
        append(target, prefix, part, false);
    }
    
    private void append(RequestBuilder<?> target, String prefix, ClauseBuilder<?> part, boolean forceNonEmpty) {
        if (part == null) return;
        String s = part.toString();
        if (s.isEmpty()) {
            if (forceNonEmpty) {
                throw new IllegalStateException(
                        "Empty " + prefix.trim() + " clause");
            }
            return;
        }
        target.append(prefix);
        target.append(s);
        target.pushArguments(part.getArguments());
    }

    @Override
    public String toString() {
        return query(queryDialect.get()).toString();
    }
    
    @Override
    public MiResultSet execute() throws MiException {
        return query().execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return query().asAction();
    }

    @Override
    public Select select() {
        return select.and();
    }

    @Override
    public From from() {
        return from;
    }

    @Override
    public Join join() {
        JoinBuilder j = new JoinBuilder();
        joins.add(j);
        return j;
    }

    @Override
    public Where where() {
        if (where == null) where = new WhereBuilder();
        return where.and();
    }

    @Override
    public GroupBy groupBy() {
        if (groupBy == null) groupBy = new GroupByBuilder();
        return groupBy.and();
    }

    @Override
    public Having having() {
        if (having == null) having = new HavingBuilder();
        return having.and();
    }

    @Override
    public OrderBy orderBy() {
        if (orderBy == null) orderBy = new OrderByBuilder();
        return orderBy.and();
    }
    
    protected class ClauseBuilder<This extends RequestBuilder<This>> extends RequestBuilderDelegator<This> implements SqlClause, RequestBuilder<This> {

        protected final RequestString queryString;
        private final String sep;
        private boolean first = true;
        private boolean addSeparator = false;

        public ClauseBuilder() {
            this.sep = null;
            this.queryString = queryDialect.get();
        }

        public ClauseBuilder(String sep) {
            this.sep = sep;
            this.queryString = queryDialect.get();
        }

        public ClauseBuilder(RequestString queryString, String sep) {
            this.queryString = queryString;
            this.sep = sep;
        }

        public This and() {
            if (sep == null) {
                throw new UnsupportedOperationException();
            }
            addSeparator = true;
            return _this();
        }

        @Override
        protected RequestBuilder<?> getDelegatee() {
            return queryString;
        }

        @Override
        protected RequestBuilder<?> getWriteDelegatee() {
            if (addSeparator) {
                addSeparator = false;
                if (!first) append(sep);
            }
            first = false;
            return queryString;
        }
        
        public List<Object> getArguments() {
            return queryString.getArguments();
        }
        
        boolean beforeFirst() {
            return first;
        }
        
        @Override
        public Select select() {
            return SqlQueryBuilder.this.select();
        }

        @Override
        public From from() {
            return SqlQueryBuilder.this.from();
        }

        @Override
        public Join join() {
            return SqlQueryBuilder.this.join();
        }

        @Override
        public Where where() {
            return SqlQueryBuilder.this.where();
        }

        @Override
        public GroupBy groupBy() {
            return SqlQueryBuilder.this.groupBy();
        }

        @Override
        public Having having() {
            return SqlQueryBuilder.this.having();
        }

        @Override
        public OrderBy orderBy() {
            return SqlQueryBuilder.this.orderBy();
        }
    }
    
    protected class SelectBuilder extends ClauseBuilder<Select> implements Select {

        public SelectBuilder() {
            super(", ");
        }
    }
    
    protected class FromBuilder extends ClauseBuilder<From> implements From {
    }
    
    protected class JoinBuilder extends ClauseBuilder<Join> implements Join {
        
        private String prefix = null;
        private WhereBuilder on = null;

        public JoinBuilder() {
        }

        @Override
        public Join and() {
            return SqlQueryBuilder.this.join();
        }

        @Override
        public Join left() {
            prefix = "LEFT";
            return _this();
        }

        @Override
        public Join right() {
            prefix = "RIGHT";
            return _this();
        }

        @Override
        public Join outer() {
            prefix = "OUTER";
            return _this();
        }

        @Override
        public Where on() {
            if (on == null) {
                ql(" ON ");
                on = new WhereBuilder(queryString);
            }
            return on;
        }

        @Override
        public String toString() {
            String s = super.toString();
            if (on != null && on.beforeFirst()) s = s.substring(0, s.length()-4);
            if (s.isEmpty()) return s;
            return prefix == null ? " JOIN " + s :
                    " " + prefix + " JOIN " + s;
        }
    }
    
    protected class WhereBuilder extends ClauseBuilder<Where> implements Where {

        public WhereBuilder() {
            super(" AND ");
        }

        public WhereBuilder(RequestString queryString) {
            super(queryString, " AND ");
        }
    }
    
    protected class GroupByBuilder extends ClauseBuilder<GroupBy> implements GroupBy {

        public GroupByBuilder() {
            super(", ");
        }
    }
    
    protected class HavingBuilder extends ClauseBuilder<Having> implements Having {

        public HavingBuilder() {
            super(" AND ");
        }
    }
    
    protected class OrderByBuilder extends ClauseBuilder<OrderBy> implements OrderBy {

        public OrderByBuilder() {
            super(", ");
        }
    }
}
