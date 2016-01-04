package org.cthul.miro.db.sql.impl;

import java.util.function.Function;
import org.cthul.miro.db.*;
import org.cthul.miro.db.impl.BasicDBStringBuilder;
import org.cthul.miro.db.impl.QlBuilderDelegator;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlJoinableClause;
import org.cthul.miro.db.sql.syntax.SqlSyntax;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public abstract class AbstractSqlStatement {
    
    private final MiConnection connection;
    private final SqlSyntax syntax;

    public AbstractSqlStatement(MiConnection connection, SqlSyntax syntax) {
        this.connection = connection;
        this.syntax = syntax;
    }

    protected <Req extends MiDBString> Req request(Function<MiConnection, Req> requestFactory) {
        close();
        return stmt(requestFactory.apply(connection));
    }

    protected SqlSyntax getSyntax() {
        return syntax;
    }
    
    protected QlBuilder<?> newQlBuilder(MiDBString coreBuilder) {
        return getSyntax().newQlBuilder(coreBuilder);
    }
    
    protected void close() {
    }
    
    @Override
    public String toString() {
        return stmt(new BasicDBStringBuilder()).toString();
    }
    
    protected <Stmt extends MiDBString> Stmt stmt(Stmt stmt) {
        buildStatement(stmt);
        return stmt;
    }
    
    protected abstract void buildStatement(MiDBString stmt);
    
    protected void append(MiDBString target, Clause part) {
        append(target, null, part, false);
    }
    
    protected void append(MiDBString target, String prefix, Clause part) {
        append(target, prefix, part, false);
    }
    
    protected void append(MiDBString target, String prefix, Clause part, boolean forceNonEmpty) {
        if (part == null) return;
        if (part.isEmpty()) {
            if (forceNonEmpty) {
                throw new IllegalStateException(
                        "Empty " + prefix.trim() + " clause");
            }
            return;
        }
        if (prefix != null) target.append(prefix);
        part.addTo(target);
    }
    
    protected interface Clause {
        boolean isEmpty();
        void addTo(MiDBString coreBuilder);
    }
    
    protected class ClauseBuilder<This extends QlBuilder<This>> 
                    extends QlBuilderDelegator<This> 
                    implements Clause {

        private final BasicDBStringBuilder coreBuilder = new BasicDBStringBuilder();
        protected final QlBuilder<?> qlBuilder;
        private final String sep;
        private final String prefix;
        private final String postfix;
        private boolean empty = true;
        private boolean open = false;

        public ClauseBuilder() {
            this(null, null, null);
        }

        public ClauseBuilder(String sep) {
            this(sep, null, null);
        }
        
        public ClauseBuilder(String sep, String prefix, String postfix) {
            super(AbstractSqlStatement.this.getSyntax());
            this.qlBuilder = newQlBuilder(coreBuilder);
            this.sep = sep;
            this.prefix = prefix;
            this.postfix = postfix;
        }

        public This and() {
            if (sep == null) {
                throw new UnsupportedOperationException();
            }
            if (open && postfix != null) {
                append(postfix);
            }
            open = false;
            return _this();
        }

        @Override
        protected QlBuilder<?> getDelegatee() {
            return qlBuilder;
        }

        @Override
        protected QlBuilder<?> getWriteDelegatee() {
            if (!open) {
                open = true;
                if (!empty && sep != null) {
                    append(sep);
                }
                empty = false;
                if (prefix != null) {
                    append(prefix);
                }
            }
            return getDelegatee();
        }
        
        @Override
        public boolean isEmpty() {
            return empty;
        }
        
        @Override
        public void addTo(MiDBString target) {
            coreBuilder.addTo(target);
            if (open && postfix != null) target.append(postfix);
        }
    }
    
    protected abstract class JoinBuilder<This extends SqlJoinableClause.Join<This>, W extends SqlJoinableClause.Where<?> & Clause> extends ClauseBuilder<This> implements SqlJoinableClause.Join<This> {
        
        private String prefix = "";
        private W on = null;

        public JoinBuilder() {
        }

        protected abstract W newOnCondition();

        @Override
        public This left() {
            prefix = "LEFT ";
            return _this();
        }

        @Override
        public This right() {
            prefix = "RIGHT ";
            return _this();
        }

        @Override
        public This outer() {
            prefix = "OUTER ";
            return _this();
        }

        @Override
        public W on() {
            if (on == null) {
                on = newOnCondition();
            }
            return on;
        }
        
        public void appendTo(SelectQueryBuilder queryBuilder) {
            Join join = queryBuilder.join();
            if (prefix.contains("LEFT")) {
                join.left();
            } else if (prefix.contains("RIGHT")) {
                join.right();
            } else if (prefix.contains("OUTER")) {
                join.outer();
            }
            super.addTo(join);
            AbstractSqlStatement.this.append(join.on(), on);
        }

        @Override
        public void addTo(MiDBString target) {
            if (!isEmpty()) {
                if (prefix != null) target.append(prefix);
                target.append("JOIN ");
                super.addTo(target);
                AbstractSqlStatement.this.append(target, " ON ", on);
            }
        }
    }
    
}
