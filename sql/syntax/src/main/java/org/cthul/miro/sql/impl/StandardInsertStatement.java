package org.cthul.miro.sql.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.request.MiUpdateBuilder;
import org.cthul.miro.sql.InsertStatement;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.AbstractStatement;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.string.SyntaxStringBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlBuilderDelegator;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SelectBuilder.From;
import org.cthul.miro.sql.SelectBuilder.GroupBy;
import org.cthul.miro.sql.SelectBuilder.Having;
import org.cthul.miro.sql.SelectBuilder.Join;
import org.cthul.miro.sql.SelectBuilder.OrderBy;
import org.cthul.miro.sql.SelectBuilder.Select;
import org.cthul.miro.sql.SelectBuilder.Where;
import org.cthul.miro.sql.InsertFromBuilder;
import org.cthul.miro.sql.InsertValuesBuilder;

/**
 *
 */
public class StandardInsertStatement 
                extends AbstractStatement<MiUpdateBuilder> 
                implements InsertStatement {
    
    private final SyntaxStringBuilder intoString;
    private final SyntaxStringBuilder defString;
    private final Into into;
    private SelectBuilder query = null;
    private Columns columns = null;
    private Values values = null;

    public StandardInsertStatement(Syntax syntax, Supplier<MiUpdateBuilder> requestFactory) {
        super(syntax, requestFactory);
    }

    public StandardInsertStatement(Syntax syntax, MiUpdateBuilder request) {
        super(syntax, request);
    }

    public StandardInsertStatement(Syntax syntax, StatementBuilder dbString, MiUpdateBuilder request) {
        super(syntax, dbString, request);
    }
    
    {
        intoString = new SyntaxStringBuilder(getSyntax());
        defString = new SyntaxStringBuilder(getSyntax());
        into = intoString.as(Into::new);
    }

    @Override
    protected void closeSubclauses() {
        intoString.close();
        defString.close();
    }
    
    protected InsertFromBuilder asInsertFrom() {
        if (columns != null) {
            throw new IllegalStateException("This is an INSERT VALUES statement");
        }
        if (query == null) {
            query = defString.begin(SqlDQML.select());
            values = new Values();
        }
        return this;
    }
    
    protected InsertFromBuilder asInsertValues() {
        if (query != null) {
            throw new IllegalStateException("This is an INSERT FROM statement");
        }
        if (columns == null) {
            columns = defString.as(Columns::new);
        }
        return this;
    }
    
    protected boolean isInsertValues() {
        return columns != null;
    }

    @Override
    protected void buildStatement(StatementBuilder stmt) {
        QlBuilder<?> ql = getSyntax().newClause(stmt, QlBuilder.TYPE);
        ql.append("INSERT INTO ");
        intoString.addTo(ql);
        if (isInsertValues()) {
            ql.append(" (");
            defString.addTo(ql);
            ql.append(")");
            values.writeTo(ql);
        } else {
            ql.append(" ");
            defString.addTo(ql);
        }
    }

    @Override
    protected StatementBuilder newToStringBuilder() {
        return new SyntaxStringBuilder(getSyntax());
    }

    @Override
    public Into into() {
        return into;
    }

    @Override
    public SelectBuilder query() {
        asInsertFrom();
        return query;
    }

    @Override
    public Columns columns() {
        asInsertValues();
        return columns;
    }

    @Override
    public Values values() {
        asInsertValues();
        return values;
    }

    @Override
    public Long execute() throws MiException {
        return request().execute();
    }

    @Override
    public MiAction<Long> asAction() {
        return request().asAction();
    }
    
    protected class Into extends QlBuilderDelegator<InsertStatement.Into> implements InsertStatement.Into {

        private final QlBuilder<?> builder;
        
        public Into(StatementBuilder parent) {
            super(StandardInsertStatement.this.getSyntax());
            this.builder = parent.begin(QlBuilder.TYPE);
        }
        
        @Override
        protected QlBuilder<?> getDelegate() {
            return builder;
        }

        @Override
        public Select select() {
            return query().select();
        }

        @Override
        public From from() {
            return query().from();
        }

        @Override
        public Join join() {
            return query().join();
        }

        @Override
        public Where where() {
            return query().where();
        }

        @Override
        public GroupBy groupBy() {
            return query().groupBy();
        }

        @Override
        public Having having() {
            return query().having();
        }

        @Override
        public OrderBy orderBy() {
            return query().orderBy();
        }

        @Override
        public Into into() {
            return this;
        }

        @Override
        public Columns columns() {
            return StandardInsertStatement.this.columns();
        }

        @Override
        public Values values() {
            return StandardInsertStatement.this.values();
        }
    }
    
    protected class Columns extends QlBuilderDelegator<InsertValuesBuilder.Columns> implements InsertValuesBuilder.Columns {

        private final QlBuilder<?> builder;
        private boolean empty = true;
        private boolean nextColumn = true;
        
        public Columns(StatementBuilder parent) {
            super(StandardInsertStatement.this.getSyntax());
            this.builder = parent.begin(QlBuilder.TYPE);
        }
        
        @Override
        protected QlBuilder<?> getDelegate() {
            return builder;
        }

        @Override
        protected QlBuilder<?> getWriteDelegate() {
            if (nextColumn) {
                nextColumn = false;
                if (empty) empty = false;
                else append(",");
            }
            return super.getWriteDelegate();
        }

        @Override
        public Columns and() {
            nextColumn = true;
            return this;
        }
        
        @Override
        public Into into() {
            return StandardInsertStatement.this.into();
        }

        @Override
        public Columns columns() {
            return StandardInsertStatement.this.columns();
        }

        @Override
        public Values values() {
            return StandardInsertStatement.this.values();
        }
    }
    
    protected class Values implements InsertValuesBuilder.Values {
        
        private final List<Object[]> values = new ArrayList<>();
        private int valueCount = -1;

        protected void addValues(Object[] v) {
            values.add(v);
        }
        
        protected void checkArgumentSize(int size) {
            if (valueCount < 0) {
                valueCount = size;
            } else if (valueCount != size) {
                throw new IllegalArgumentException(
                        "Expected " + valueCount + " values, got " + size);
            }
        }

        @Override
        public Values add(Object... values) {
            checkArgumentSize(values.length);
            addValues(values);
            return this;
        }
        
        @Override
        public Values add(Iterable<?> values) {
            Collection<?> collection;
            if (values instanceof Collection) {
                collection = (Collection<?>) values;
            } else {
                List<Object> bag = new ArrayList<>();
                values.forEach(bag::add);
                collection = bag;
            }
            checkArgumentSize(collection.size());
            addValues(collection.toArray());
            return this;
        }
        
        protected void writeTo(MiDBString stmt) {
            stmt.append(" VALUES (");
            for (int i = 0; i < valueCount; i++) {
                stmt.append(i == 0 ? "?" : ",?");
            }
            stmt.append(")");
        }
        
        protected void addData(MiUpdateBuilder stmt) {
            values.forEach(batch -> {
                stmt.begin(MiDBString.TYPE).pushArguments(batch);
                stmt.addBatch();
            });
        }

        @Override
        public Into into() {
            return StandardInsertStatement.this.into();
        }

        @Override
        public Columns columns() {
            return StandardInsertStatement.this.columns();
        }

        @Override
        public Values values() {
            return StandardInsertStatement.this.values();
        }
    }
}
