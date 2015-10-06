package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.BasicCoreStmtBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.cthul.miro.db.*;
import org.cthul.miro.db.sql.syntax.SqlSyntax;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlBuilderDelegator;
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

    protected <Req extends CoreStmtBuilder> Req request(Function<MiConnection, Req> requestFactory) {
        return stmt(requestFactory.apply(connection));
    }

    protected SqlSyntax getSyntax() {
        return syntax;
    }
    
    protected QlBuilder<?> newQlBuilder(CoreStmtBuilder coreBuilder) {
        return getSyntax().newQlBuilder(coreBuilder);
    }
    
    @Override
    public String toString() {
        return stmt(new BasicCoreStmtBuilder()).toString();
    }
    
    protected <Stmt extends CoreStmtBuilder> Stmt stmt(Stmt stmt) {
        buildStatement(stmt);
        return stmt;
    }
    
    protected abstract void buildStatement(CoreStmtBuilder stmt);
    
    protected void append(CoreStmtBuilder target, Clause part) {
        append(target, null, part, false);
    }
    
    protected void append(CoreStmtBuilder target, String prefix, Clause part) {
        append(target, prefix, part, false);
    }
    
    protected void append(CoreStmtBuilder target, String prefix, Clause part, boolean forceNonEmpty) {
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
        void addTo(CoreStmtBuilder coreBuilder);
    }
    
    protected class ClauseBuilder<This extends QlBuilder<This>> 
                    extends QlBuilderDelegator<This> 
                    implements Clause {

        private final BasicCoreStmtBuilder coreBuilder = new BasicCoreStmtBuilder();
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
        public void addTo(CoreStmtBuilder target) {
            coreBuilder.addTo(target);
            if (open && postfix != null) target.append(postfix);
        }
    }
    
    protected abstract class JoinBuilder<This extends SqlJoinableClause.Join<This>, W extends SqlJoinableClause.Where<?> & Clause> extends ClauseBuilder<This> implements SqlJoinableClause.Join<This> {
        
        private String prefix = null;
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

        @Override
        public void addTo(CoreStmtBuilder target) {
            if (!isEmpty()) {
                if (prefix != null) target.append(prefix);
                target.append("JOIN ");
                super.addTo(target);
                AbstractSqlStatement.this.append(target, " ON ", on);
            }
        }
    }
    
}
