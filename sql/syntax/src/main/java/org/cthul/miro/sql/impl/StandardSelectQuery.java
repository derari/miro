package org.cthul.miro.sql.impl;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SelectBuilder.Join;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.request.MiQueryString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class StandardSelectQuery extends AbstractSqlStatement<MiQueryString> implements SelectQuery {
    
    private SelectClBuilder select = null;
    private FromBuilder from = null;
    private final List<JoinBuilder> joins = new ArrayList<>();
    private WhereBuilder where = null;
    private GroupByBuilder groupBy = null;
    private HavingBuilder having = null;
    private OrderByBuilder orderBy = null;

    public StandardSelectQuery(Syntax syntax, MiConnection connection) {
        super(syntax, connection::newQuery);
    }

    public StandardSelectQuery(Syntax syntax, MiQueryString dbString) {
        super(syntax, dbString);
    }

    public StandardSelectQuery(Syntax syntax, MiDBString dbString, MiQueryString request) {
        super(syntax, dbString, request);
    }
    
    @Override
    protected void closeSubclauses() {
        if (select != null) select.close();
        if (from != null) from.close();
        joins.forEach(JoinBuilder::close);
        if (where != null) where.close();
        if (groupBy != null) groupBy.close();
        if (having != null) having.close();
        if (orderBy != null) orderBy.close();
    }

    @Override
    protected void buildStatement(MiDBString stmt) {
        append(stmt, "SELECT ", select);
        append(stmt, " FROM ", from);
        joins.forEach(j -> append(stmt, " ", j));
        append(stmt, " WHERE ", where);
        append(stmt, " GROUP BY ", groupBy);
        append(stmt, " HAVING ", having);
        append(stmt, " ORDER BY ", orderBy);
    }
    
    @Override
    public MiResultSet execute() throws MiException {
        return request().execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return request().asAction();
    }

    public void appendTo(SelectBuilder sql) {
        append(sql.select(), select);
        append(sql.from(), from);
        joins.forEach(j -> j.appendTo(sql));
        append(sql.where(), where);
        append(sql.groupBy(), groupBy);
        append(sql.having(), having);
        append(sql.orderBy(), orderBy);
    }

    @Override
    public Select select() {
        if (select == null) select = newSelectBuilder();
        return select.and();
    }

    protected SelectClBuilder newSelectBuilder() {
        return new SelectClBuilder();
    }

    @Override
    public From from() {
        if (from == null) from = newFromBuilder();
        return from;
    }

    protected FromBuilder newFromBuilder() {
        return new FromBuilder();
    }

    @Override
    public Join join() {
        JoinBuilder j = newJoinBuilder();
        joins.add(j);
        return j;
    }

    protected JoinBuilder newJoinBuilder() {
        return new JoinBuilder();
    }

    @Override
    public Where where() {
        if (where == null) where = newWhereBuilder();
        return where.and();
    }

    protected WhereBuilder newWhereBuilder() {
        return new WhereBuilder();
    }

    @Override
    public GroupBy groupBy() {
        if (groupBy == null) groupBy = newGroupByBuilder();
        return groupBy.and();
    }

    protected GroupByBuilder newGroupByBuilder() {
        return new GroupByBuilder();
    }

    @Override
    public Having having() {
        if (having == null) having = newHavingBuilder();
        return having.and();
    }

    protected HavingBuilder newHavingBuilder() {
        return new HavingBuilder();
    }

    @Override
    public OrderBy orderBy() {
        if (orderBy == null) orderBy = newOrderByBuilder();
        return orderBy.and();
    }

    protected OrderByBuilder newOrderByBuilder() {
        return new OrderByBuilder();
    }
    
    protected class ClauseBuilder<This extends QlBuilder<This>> extends AbstractSqlStatement<MiQueryString>.ClauseBuilder<This> implements SelectBuilder {

        public ClauseBuilder() {
        }

        public ClauseBuilder(String sep) {
            super(sep);
        }

        public ClauseBuilder(String sep, String prefix, String postfix) {
            super(sep, prefix, postfix);
        }

        @Override
        public Select select() {
            return StandardSelectQuery.this.select();
        }

        @Override
        public From from() {
            return StandardSelectQuery.this.from();
        }

        @Override
        public Join join() {
            return StandardSelectQuery.this.join();
        }

        @Override
        public Where where() {
            return StandardSelectQuery.this.where();
        }

        @Override
        public GroupBy groupBy() {
            return StandardSelectQuery.this.groupBy();
        }

        @Override
        public Having having() {
            return StandardSelectQuery.this.having();
        }

        @Override
        public OrderBy orderBy() {
            return StandardSelectQuery.this.orderBy();
        }
    }
    
    protected class SelectClBuilder extends ClauseBuilder<Select> implements Select {

        public SelectClBuilder() {
            super(", ");
        }
    }
    
    protected class FromBuilder extends ClauseBuilder<From> implements From {
    }
    
    protected class JoinBuilder extends AbstractSqlStatement<MiQueryString>.JoinBuilder<Join, WhereBuilder> implements Join {

        @Override
        protected WhereBuilder newOnCondition() {
            return newWhereBuilder();
        }

        @Override
        public Select select() {
            return StandardSelectQuery.this.select();
        }

        @Override
        public From from() {
            return StandardSelectQuery.this.from();
        }

        @Override
        public StandardSelectQuery.Join join() {
            return StandardSelectQuery.this.join();
        }

        @Override
        public StandardSelectQuery.Where where() {
            return StandardSelectQuery.this.where();
        }

        @Override
        public GroupBy groupBy() {
            return StandardSelectQuery.this.groupBy();
        }

        @Override
        public Having having() {
            return StandardSelectQuery.this.having();
        }

        @Override
        public OrderBy orderBy() {
            return StandardSelectQuery.this.orderBy();
        }
    }
    
    protected class WhereBuilder extends ClauseBuilder<Where> implements Where {

        public WhereBuilder() {
            super(" AND ");
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
