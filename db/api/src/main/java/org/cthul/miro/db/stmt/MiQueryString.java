package org.cthul.miro.db.stmt;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A query string builder.
 */
public interface MiQueryString extends MiQuery, MiDBString {

    @Override
    MiQueryString append(CharSequence chars);
    
    static RequestType<MiQueryString> TYPE = new RequestType<MiQueryString>() {
        @Override
        public MiQueryString createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return cnn.newQuery();
        }
    };
}
