package org.cthul.miro.query;

import org.cthul.miro.query.api.InternalQueryBuilder;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SimpleQueryPart;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.util.SqlUtils;

public class SqlQueryBuilder<This extends SqlQueryBuilder<This>> implements SqlParser<This> {

    private final InternalQueryBuilder queryBuilder;

    public SqlQueryBuilder(InternalQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
    
    protected This self() {
        return (This) this;
    }
    
    protected String newKey(String hint) {
        return queryBuilder.newKey(hint);
    }
    
    protected void addPart(QueryPartType type, QueryPart part) {
        queryBuilder.addPart((QueryPartType) type, part);
    }

    @Override
    public This select(String... selectClause) {
        for (String s: selectClause) {
            select(s);
        }
        return self();
    }
    
    @Override
    public This select(String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            QueryPart sp = new SimpleQueryPart(part[0], part[1]);
            addPart(DataQueryPart.SELECT, sp);
        }
        return self();
    }
    
    public This select(QueryPart part) {
        addPart(DataQueryPart.SELECT, part);
        return self();
    }

    @Override
    public This from(String... fromClause) {
        for (String f: fromClause) {
            from(f);
        }
        return self();
    }

    @Override
    public This table(String fromClause) {
        String[] part = SqlUtils.parseFromPart(fromClause);
        String key = part[0] != null ?  part[0] : newKey("from");
        return table(new SimpleQueryPart(key, part[1]));
    }
    
    public This table(QueryPart part) {
        addPart(DataQueryPart.TABLE, part);
        return self();
    }

    @Override
    public This attributes(String... attributes) {
        for (String a: attributes) {
//            addPart(DataQueryPart.VALUES, new SimplePart.Attribute(a));
        }
        return self();
    }

    @Override
    public This from(String fromClause) {
        String[] part = SqlUtils.parseFromPart(fromClause);
        String key = part[0] != null ?  part[0] : newKey("from");
        return from(new SimpleQueryPart(key, part[1]));
    }
    
    public This from(QueryPart part) {
        addPart(DataQueryPart.SUBQUERY, part);
        return self();
    }

    @Override
    public This join(String... joinClause) {
        for (String j: joinClause) {
            join(j);
        }
        return self();
    }

    @Override
    public This join(String joinClause) {
        String[] part = SqlUtils.parseJoinPart(joinClause);
        return join(new SimpleQueryPart(part[0], part[1]));
    }
    
    public This join(QueryPart part) {
        addPart(DataQueryPart.JOIN, part);
        return self();
    }

    @Override
    public This where(String whereClause) {
        return where(newKey("where"), whereClause);
    }

    @Override
    public This where(String key, String whereClause) {
        return where(new SimpleQueryPart(key, whereClause));
    }
    
    public This where(QueryPart part) {
        addPart(DataQueryPart.WHERE, part);
        return self();
    }
}
