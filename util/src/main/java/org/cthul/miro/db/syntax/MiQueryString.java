package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiQuery;

/**
 *
 */
public interface MiQueryString extends MiQuery, CoreStmtBuilder {

    @Override
    MiQueryString append(CharSequence chars);
    
//    static final Type TYPE = new Type();
//    
//    static class Type implements RequestType<MiQueryString> {
//        @Override
//        public MiQueryString newStatement(MiConnection connection) {
//            return connection.newQuery();
//        }
//    }
}
