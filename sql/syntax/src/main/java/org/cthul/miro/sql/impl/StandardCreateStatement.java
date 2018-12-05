package org.cthul.miro.sql.impl;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.impl.AbstractStatement;
import org.cthul.miro.sql.CreateStatement;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.request.MiUpdateString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.sql.CreateTableBuilder;
import org.cthul.miro.db.syntax.AutocloseableBuilder;

/**
 *
 */
public class StandardCreateStatement extends AbstractStatement<MiUpdateString> implements CreateStatement, AutocloseableBuilder {
    
//    private final MiConnection cnn;
//    private final MiUpdateString dbString;
//    private final Syntax syntax;
    private Table table = null;

    public StandardCreateStatement(MiConnection cnn, Syntax syntax) {
        super(syntax, cnn::newUpdate);
    }

    public StandardCreateStatement(Syntax syntax, MiUpdateString request) {
        super(syntax, request);
    }

    public StandardCreateStatement(Syntax syntax, MiDBString dbString, MiUpdateString request) {
        super(syntax, dbString, request);
    }

    @Override
    public Table table(QlCode name) {
        if (table != null) {
            throw new IllegalStateException();
        }
        return table = newTable(name);
    }
    
    protected Table newTable(QlCode name) {
        return new Table(name);
    }
    
    public Table getTable() {
        if (table == null) {
            throw new IllegalStateException("Table name required");
        }
        return table;
    }

    @Override
    protected void buildStatement(MiDBString stmt) {
        if (table != null) {
            getTable().buildStatement(stmt);
        }
    }

    @Override
    public Long execute() throws MiException {
        getTable();
        return request().execute();
    }

    @Override
    public MiAction<Long> asAction() {
        getTable();
        return request().asAction();
    }
    
    protected class Table implements CreateStatement.Table {

        protected final QlCode name;
        private final List<StandardCreateStatement.Column> columns = new ArrayList<>();

        public Table(QlCode name) {
            this.name = name;
        }

        @Override
        public Column column(QlCode name) {
            StandardCreateStatement.Column c = newColumn(name);
            columns.add(c);
            return c;
        }
        
        protected StandardCreateStatement.Column newColumn(QlCode name) {
            return new StandardCreateStatement.Column(this, name);
        }

        protected void buildStatement(MiDBString stmt) {
            QlBuilder<?> ql = QlBuilder.create(getSyntax(), stmt);
            begin(ql);
            body(ql);
            end(ql);
        }

        protected QlCode getName() {
            return name;
        }

        protected List<StandardCreateStatement.Column> getColumns() {
            return columns;
        }
        
        protected void begin(QlBuilder<?> ql) {
            ql.append("CREATE TABLE ")
              .append(getName())
              .append(" (");
        }
        
        protected void body(QlBuilder<?> ql) {
            columns(ql, getColumns());
        }
        
        protected void columns(QlBuilder<?> ql, List<StandardCreateStatement.Column> columns) {
            boolean first = true;
            for (StandardCreateStatement.Column c: columns) {
                if (first) first = false;
                else betweenColumns(ql);
                column(ql, c);
            }
        }
        
        protected void column(QlBuilder<?> ql, StandardCreateStatement.Column column) {
            ql.append(column);
        }
        
        protected void betweenColumns(QlBuilder<?> ql) {
            ql.append(", ");
        }
        
        protected void end(QlBuilder<?> ql) {
            ql.append(")");
        }

        @Override
        public Long execute() throws MiException {
            return StandardCreateStatement.this.execute();
        }

        @Override
        public MiAction<Long> asAction() {
            return StandardCreateStatement.this.asAction();
        }

        @Override
        public String toString() {
            return StandardCreateStatement.this.toString();
        }
    }
    
    protected static class Column implements CreateTableBuilder.Column, QlCode {
        
        private final CreateTableBuilder owner;
        private final QlCode name;
        private String type = "INTEGER";
        private Integer scale = null;
        private boolean nullable = true;
        private boolean primaryKey = false;
        private boolean autoGenerate = false;

        public Column(CreateTableBuilder owner, QlCode name) {
            this.owner = owner;
            this.name = name;
        }

        public QlCode getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Integer getScale() {
            return scale;
        }

        public boolean isNullable() {
            return nullable;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public boolean isAutoGenerate() {
            return autoGenerate;
        }

        @Override
        public Column type(String name, Integer scale) {
            this.type = name;
            this.scale = scale;
            return this;
        }

        @Override
        public Column nullable(boolean isNullable) {
            this.nullable = isNullable;
            return this;
        }

        @Override
        public Column primaryKey(boolean isKey) {
            this.primaryKey = isKey;
            return this;
        }

        @Override
        public Column autoGenerate(boolean generate) {
            this.autoGenerate = generate;
            return this;
        }

        @Override
        public Column column(QlCode name) {
            return owner.column(name);
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            writeDeclaration(qlBuilder);
        }
        
        protected void writeDeclaration(QlBuilder<?> query) {
            query.append(getName());
            writeType(query);
            writeAutGenerate(query);
            writePrimaryKey(query);
        }
        
        public void writeType(QlBuilder<?> query) {
            query.append(" ").append(type);
            Integer myScale = getScale();
            if (myScale != null) {
                query.append("(").append(myScale.toString()).append(")");
            }
        }
        
        public void writeNullable(QlBuilder<?> query) {
            query.append(isNullable() ? " NULL" : " NOT NULL");
        }
        
        public void writeAutGenerate(QlBuilder<?> query) {
            if (isAutoGenerate()) {
                query.append(" GENERATED BY DEFAULT AS IDENTITY");
            }
        }
        
        public void writePrimaryKey(QlBuilder<?> query) {
            if (isPrimaryKey()) {
                query.append(" PRIMARY KEY");
            }
        }
    }
}
