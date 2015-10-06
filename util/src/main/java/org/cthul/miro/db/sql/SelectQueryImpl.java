package org.cthul.miro.db.sql;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.db.sql.SelectQueryBuilder.Join;
import org.cthul.miro.db.sql.syntax.SqlSyntax;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class SelectQueryImpl extends AbstractSqlStatement implements SelectQuery {
    
    private SelectBuilder select = null;
    private FromBuilder from = null;
    private final List<JoinBuilder> joins = new ArrayList<>();
    private WhereBuilder where = null;
    private GroupByBuilder groupBy = null;
    private HavingBuilder having = null;
    private OrderByBuilder orderBy = null;

    public SelectQueryImpl(MiConnection connection, SqlSyntax syntax) {
        super(connection, syntax);
    }

    @Override
    protected void buildStatement(CoreStmtBuilder stmt) {
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
        return request(MiConnection::newQuery).execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return request(MiConnection::newQuery).asAction();
    }

    @Override
    public Select select() {
        if (select == null) select = newSelectBuilder();
        return select.and();
    }

    protected SelectBuilder newSelectBuilder() {
        return new SelectBuilder();
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
    
    protected class ClauseBuilder<This extends QlBuilder<This>> extends AbstractSqlStatement.ClauseBuilder<This> implements SelectQueryBuilder {

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
            return SelectQueryImpl.this.select();
        }

        @Override
        public From from() {
            return SelectQueryImpl.this.from();
        }

        @Override
        public Join join() {
            return SelectQueryImpl.this.join();
        }

        @Override
        public Where where() {
            return SelectQueryImpl.this.where();
        }

        @Override
        public GroupBy groupBy() {
            return SelectQueryImpl.this.groupBy();
        }

        @Override
        public Having having() {
            return SelectQueryImpl.this.having();
        }

        @Override
        public OrderBy orderBy() {
            return SelectQueryImpl.this.orderBy();
        }
    }
    
    protected class SelectBuilder extends ClauseBuilder<Select> implements Select {

        public SelectBuilder() {
            super(", ");
        }
    }
    
    protected class FromBuilder extends ClauseBuilder<From> implements From {
    }
    
    protected class JoinBuilder extends AbstractSqlStatement.JoinBuilder<Join, WhereBuilder> implements Join {

        @Override
        protected WhereBuilder newOnCondition() {
            return newWhereBuilder();
        }

        @Override
        public Select select() {
            return SelectQueryImpl.this.select();
        }

        @Override
        public From from() {
            return SelectQueryImpl.this.from();
        }

        @Override
        public SelectQueryImpl.Join join() {
            return SelectQueryImpl.this.join();
        }

        @Override
        public SelectQueryImpl.Where where() {
            return SelectQueryImpl.this.where();
        }

        @Override
        public GroupBy groupBy() {
            return SelectQueryImpl.this.groupBy();
        }

        @Override
        public Having having() {
            return SelectQueryImpl.this.having();
        }

        @Override
        public OrderBy orderBy() {
            return SelectQueryImpl.this.orderBy();
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
