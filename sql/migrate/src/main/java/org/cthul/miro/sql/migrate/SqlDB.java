package org.cthul.miro.sql.migrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.AbstractQlBuilder;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.string.SyntaxStringBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.util.Cache;

/**
 *
 */
public class SqlDB {
    
//    private final MiConnection cnn;
    private final Map<String, Schema> schemas;
    private final List<Table> tableOrder = new ArrayList<>();
    private final Map<String, Table> tables;

    public SqlDB(MiConnection cnn) {
//        this.cnn = cnn;
        schemas = Cache.map(n -> new Schema(this, n));
        tables = Cache.map(n -> {
            Table t = new Table(cnn, n);
            tableOrder.add(t);
            return t;
        });
    }
    
    public void flush() throws MiException {
        for (Table table: tableOrder) {
            table.flush();
        }
    }
    
    public Schema schema(String name) {
        return schemas.get(name);
    }
    
    public Schema schema(QlCode code) {
        Schema s = schema(toString(code));
        s.setName(code);
        return s;
    }
    
    public Schema defaultSchema() {
        return schema("");
    }
    
    public Table table(String name) {
        return tables.get(name);
    }
    
    public Table table(QlCode code) {
        Table t = table(toString(code));
        t.setName(code);
        return t;
    }
    
    
    static String toString(QlCode code) {
        return new SyntaxStringBuilder(TO_STRING_SYNTAX).begin(QlBuilder.TYPE)
                .append(code).toString();
    }
    
    protected static final Syntax TO_STRING_SYNTAX = new Syntax() {
        public QlBuilder<?> newQlBuilder(StatementBuilder stmt) {
            class Ql extends AbstractQlBuilder<Ql> {
                public Ql(Syntax syntax, MiDBString coreBuilder) {
                    super(syntax, coreBuilder);
                }
                @Override
                public Ql identifier(String id) {
                    return append(id);
                }
                @Override
                public Ql stringLiteral(String string) {
                    return append("'").append(string).append("'");
                }
            }
            return new Ql(this, stmt.begin(MiDBString.TYPE));
        }

        @Override
        public <Cls> Cls newClause(StatementBuilder stmt, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
            if (type == QlBuilder.TYPE) {
                return type.cast(newQlBuilder(stmt));
            }
            return Syntax.super.newClause(stmt, type, onDefault); //To change body of generated methods, choose Tools | Templates.
        }
    };
}
