package org.cthul.miro.query.syntax;

public enum DataQueryType implements QueryType {
    
    INSERT,
    SELECT,
    UPDATE,
    DELETE;
    
    public SqlBuilder builder(SqlSyntax syntax) {
        return syntax.newQuery(this);
    }
}
