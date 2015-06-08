package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.AbstactRequestString;
import org.cthul.miro.db.syntax.RequestString;

public class AnsiSqlRequest extends AbstactRequestString {

    @Override
    public RequestString identifier(String id) {
        return ql("\"").ql(id).ql("\"");
    }

    @Override
    public RequestString stringLiteral(String string) {
        return ql("'")
                .ql(string.replace("\\", "\\\\")
                          .replace("'", "\\'"))
                .ql("'");
    }
}
