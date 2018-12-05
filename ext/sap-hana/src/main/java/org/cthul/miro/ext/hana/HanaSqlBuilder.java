package org.cthul.miro.ext.hana;

import org.cthul.miro.db.impl.AbstractQlBuilder;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class HanaSqlBuilder extends AbstractQlBuilder<HanaSqlBuilder> {

    public HanaSqlBuilder(Syntax syntax, MiDBString dbString) {
        super(syntax, dbString);
    }

    @Override
    public HanaSqlBuilder identifier(String id) {
        return append("\"").append(id).append("\"");
    }

    @Override
    public HanaSqlBuilder stringLiteral(String string) {
        return append("'").append(
                string.replace("\\", "\\\\")
                      .replace("'", "\\'")
        ).append("'");
    }
}
