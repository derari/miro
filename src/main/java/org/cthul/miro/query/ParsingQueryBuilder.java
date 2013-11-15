package org.cthul.miro.query;

import org.cthul.miro.util.SqlUtils;

/**
 *
 */
public class ParsingQueryBuilder extends ZQueryBuilder {

    public ParsingQueryBuilder() {
    }

    protected void sql_select(String... selectClause) {
        for (String s: selectClause) {
            sql_select(s);
        }
    }
    
    protected void sql_select(String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            QueryPart sp = new CustomPart(part[0], PartType.SELECT, part[1]);
            select(sp);
        }
    }
    
    protected QueryPart sql_from(String from) {
        QueryPart fp = new CustomPart("$$from", PartType.FROM, from);
        return from(fp);
    }
    
    protected QueryPart sql_join(String join) {
        String[] part = SqlUtils.parseJoinPart(join);
        QueryPart jp = new CustomPart(part[0], PartType.JOIN, part[1]);
        return join(jp);
    }
    
    protected QueryPart sql_where(String where) {
        String id = "$$where" + whereParts().size();
        QueryPart jp = new CustomPart(id, PartType.WHERE, where);
        return where(jp);
    }

    protected QueryPart sql_groupBy(String groupBy) {
        String id = "$$group" + groupParts().size();
        QueryPart jp = new CustomPart(id, PartType.GROUP, groupBy);
        return groupBy(jp);
    }

    protected QueryPart sql_having(String having) {
        String id = "$$having" + havingParts().size();
        QueryPart jp = new CustomPart(id, PartType.HAVING, having);
        return having(jp);
    }

    protected QueryPart sql_orderBy(String order) {
        String id = "$$order" + orderParts().size();
        QueryPart jp = new CustomPart(id, PartType.ORDER, order);
        return orderBy(jp);
    }
}
