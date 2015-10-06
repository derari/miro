package org.cthul.miro.db.syntax;

/**
 * Interface for building queries from identifiers, string literals,
 * and native query language.
 * 
 * @param <This>
 */
public interface QlBuilder<This extends QlBuilder<This>> extends CoreStmtBuilder, StatementBuilder {
    
    @Override
    This append(CharSequence query);
    
    default This ql(String query) {
        return append(query);
    }
    
    This identifier(String id);
    
    default This id(String id) {
        return identifier(id);
    }
    
    default This id(String... id) {
        This me = (This) this;
        for (int i = 0; i < id.length; i++) {
            if (i > 0) me = me.ql(".");
            me = me.id(id[i]);
        }
        return me;
    }
    
    default This namedTable(String table, String name) {
        return id(table).ql(" ").id(name);
    }
    
    default This attribute(String id, String attribute) {
        return id(id, attribute);
    }
    
    This stringLiteral(String string);
    
//    This clearArguments();
    
    @Override
    This pushArgument(Object arg);
    
//    This setArgument(int index, Object arg);
    
    public static final ClauseType<QlBuilder<?>> TYPE = new ClauseType<QlBuilder<?>>() {};
}
