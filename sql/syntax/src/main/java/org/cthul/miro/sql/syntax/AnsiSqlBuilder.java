package org.cthul.miro.sql.syntax;

import java.util.Collections;
import java.util.Map;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.AbstractQlBuilder;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class AnsiSqlBuilder extends AbstractQlBuilder<AnsiSqlBuilder> {
    
    private final Map<String, String> tableSchemas;

    public AnsiSqlBuilder(Syntax syntax, MiDBString dbString) {
        this(syntax, Collections.emptyMap(), dbString);
    }

    public AnsiSqlBuilder(Syntax syntax, Map<String, String> tableSchemas, MiDBString dbString) {
        super(syntax, dbString);
        this.tableSchemas = tableSchemas;
    }

    public AnsiSqlBuilder(Syntax syntax, StatementBuilder stmt) {
        this(syntax, Collections.emptyMap(), stmt);
    }

    public AnsiSqlBuilder(Syntax syntax, Map<String, String> tableSchemas, StatementBuilder stmt) {
        super(syntax, stmt);
        this.tableSchemas = tableSchemas;
    }

    @Override
    public AnsiSqlBuilder identifier(String id) {
        String s = tableSchemas.get(id);
        if (s != null) {
            writeId(s);
            append(".");
        }
        writeId(id);
        return this;
    }
    
    private void writeId(String id) {
        append("\"");
        append(id.replace("\\", "\\\\")
                 .replace("\"", "\\\""));
        append("\"");
    }

    @Override
    public AnsiSqlBuilder stringLiteral(String string) {
        return append("'").append(
                string.replace("\\", "\\\\")
                      .replace("'", "\\'")
            ).append("'");
    }
}
