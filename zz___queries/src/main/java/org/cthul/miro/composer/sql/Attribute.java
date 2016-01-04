package org.cthul.miro.composer.sql;

import java.util.function.Consumer;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public abstract class Attribute {
    
    private final String key;

    public Attribute(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    public void writeAlias(QlBuilder<?> ql) {
        ql.id(getKey());
    }
    
    public void writeSelectClause(QlBuilder<?> ql) {
        writeSelector(ql);
        ql.ql(" AS ");
        writeAlias(ql);
    }
    
    protected abstract void writeSelector(QlBuilder<?> ql);
    
    public static Attribute forColumn(String key, String table, String column, String name) {
        return new Column(key, table, column, name);
    }
    
    public static Attribute withExpression(String key, Consumer<? super QlBuilder<?>> expression) {
        return new Expression(key, expression);
    }
    
    protected static class Column extends Attribute {

        private final String table;
        private final String column;
        private final String name;

        public Column(String key, String table, String column, String name) {
            super(key);
            this.table = table;
            this.column = column;
            this.name = name;
        }
        
        @Override
        protected void writeSelector(QlBuilder<?> ql) {
            if (table == null) {
                ql.id(column);
            } else {
                ql.attribute(table, column);
            }
            if (name != null) {
                ql.ql(" AS ").id(name);
            }
        }
    }
    
    protected static class Expression extends Attribute {
        
        private final Consumer<? super QlBuilder<?>> expression;

        public Expression(String key, Consumer<? super QlBuilder<?>> expression) {
            super(key);
            this.expression = expression;
        }

        @Override
        protected void writeSelector(QlBuilder<?> ql) {
            expression.accept(ql);
        }
    }
}
