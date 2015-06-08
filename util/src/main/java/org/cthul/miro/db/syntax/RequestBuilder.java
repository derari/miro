package org.cthul.miro.db.syntax;

/**
 * Interface for building queries.
 * 
 * @param <This>
 */
public interface RequestBuilder<This extends RequestBuilder<This>> {
    
    This append(String query);
    
    default This ql(String query) {
        return append(query);
    }
    
    This identifier(String id);
    
    default This id(String id) {
        return identifier(id);
    }
    
    default This namedTable(String table, String name) {
        return id(table).ql(" ").id(name);
    }
    
    default This attribute(String id, String attribute) {
        return id(id).ql(".").id(attribute);
    }
    
    This stringLiteral(String string);
    
    This clearArguments();
    
    This pushArgument(Object arg);
    
    default This pushArguments(Object... args) {
        @SuppressWarnings("unchecked")
        This me = (This) this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
    
    default This pushArguments(Iterable<Object> args) {
        @SuppressWarnings("unchecked")
        This me = (This) this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
    
    This setArgument(int index, Object arg);
}
