package org.cthul.miro.composer.sql;

import java.util.function.Consumer;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public abstract class Attribute {
    
    private final String key;
    private final boolean isInternal;

    public Attribute(String key, boolean isInternal) {
        this.key = key;
        this.isInternal = isInternal;
    }

    public String getKey() {
        return key;
    }

    public boolean isInternal() {
        return isInternal;
    }
    
    public void writeAlias(QlBuilder<?> ql) {
        ql.id(getKey());
    }
    
    public void writeSelectClause(QlBuilder<?> ql) {
        writeSelector(ql);
        ql.ql(" AS ");
        writeAlias(ql);
    }
    
    public QlCode expression() {
        return ql -> writeSelector(ql);
    }
    
    protected abstract void writeSelector(QlBuilder<?> ql);
    
    public static Attribute forColumn(String key, boolean isInternal, String table, String column, String name) {
        return new Column(key, isInternal, table, column, name);
    }
    
    public static Attribute withExpression(String key, boolean isInternal, Consumer<? super QlBuilder<?>> expression) {
        return new Expression(key, isInternal, expression);
    }
    
    protected static class Column extends Attribute {

        private final String table;
        private final String column;
        private final String name;

        public Column(String key, boolean isInternal, String table, String column, String name) {
            super(key, isInternal);
            this.table = table;
            this.column = column;
            this.name = name;
        }
        
        @Override
        protected void writeSelector(QlBuilder<?> ql) {
            if (table == null) {
                ql.id(column);
            } else {
                ql.id(table, column);
            }
//            if (name != null) {
//                ql.ql(" AS ").id(name);
//            }
        }
    }
    
    protected static class Expression extends Attribute {
        
        private final Consumer<? super QlBuilder<?>> expression;

        public Expression(String key, boolean isInternal, Consumer<? super QlBuilder<?>> expression) {
            super(key, isInternal);
            this.expression = expression;
        }

        @Override
        protected void writeSelector(QlBuilder<?> ql) {
            expression.accept(ql);
        }
    }
}
