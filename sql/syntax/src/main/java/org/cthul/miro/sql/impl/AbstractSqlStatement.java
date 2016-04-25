package org.cthul.miro.sql.impl;

import java.util.function.Supplier;
import org.cthul.miro.db.impl.AbstractStatement;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.db.impl.QlBuilderDelegator;
import org.cthul.miro.sql.SqlJoinableClause;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.sql.SelectBuilder;

/**
 *
 * @param <Request>
 */
public abstract class AbstractSqlStatement<Request extends MiDBString> extends AbstractStatement<Request> {

    public AbstractSqlStatement(Syntax syntax, Supplier<Request> requestFactory) {
        super(syntax, requestFactory);
    }

    public AbstractSqlStatement(Syntax syntax, Request dbString) {
        super(syntax, dbString);
    }

    public AbstractSqlStatement(Syntax syntax, MiDBString dbString, Request request) {
        super(syntax, dbString, request);
    }
    
    protected class ClauseBuilder<This extends QlBuilder<This>> 
                    extends QlBuilderDelegator<This> 
                    implements SubClause {

        private final MiDBStringBuilder coreBuilder = new MiDBStringBuilder();
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
    
    protected abstract class JoinBuilder<This extends SqlJoinableClause.Join<This>, W extends SqlJoinableClause.Where<?> & SubClause> extends ClauseBuilder<This> implements SqlJoinableClause.Join<This> {
        
        private JoinType joinType = JoinType.INNER;
        private W on = null;

        public JoinBuilder() {
        }

        protected abstract W newOnCondition();

        @Override
        public This as(JoinType jt) {
            joinType = jt;
            return _this();
        }

        @Override
        public W on() {
            if (on == null) {
                on = newOnCondition();
            }
            return on;
        }
        
        public void appendTo(SelectBuilder queryBuilder) {
            Join join = queryBuilder.join();
            join.as(joinType);
            super.addTo(join);
            AbstractSqlStatement.this.append(join.on(), on);
        }

        @Override
        public void addTo(MiDBString target) {
            if (!isEmpty()) {
                if (joinType != JoinType.INNER) {
                    target.append(joinType.toString());
                    target.append(" ");
                }
                target.append("JOIN ");
                super.addTo(target);
                AbstractSqlStatement.this.append(target, " ON ", on);
            }
        }
    }
}
