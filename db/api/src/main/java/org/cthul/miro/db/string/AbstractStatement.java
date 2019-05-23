package org.cthul.miro.db.string;

import java.util.function.Supplier;
import org.cthul.miro.db.request.AutocloseableBuilder;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A complex statement consisting of several sub-clauses.
 * Can be used as a request or a clause.
 * @param <Builder>
 */
public abstract class AbstractStatement<Builder extends StatementBuilder> implements AutocloseableBuilder {
    
    private final Supplier<Builder> newBuilder;
    private final StatementBuilder owner;
    private final Syntax syntax;
    private boolean closed = false;

    /**
     * Creates a statement that can be executed multiple times.
     * @param newBuilder
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Supplier<? extends Builder> newBuilder) {
        this.newBuilder = () -> {
            closeSubclauses();
            Builder r = newBuilder.get();
            return _buildStatement(r);
        };
        this.owner = null;
        this.syntax = syntax;
    }

    /**
     * Creates statement as a request-clause that can be executed only once.
     * @param statement
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Builder statement) {
        this(syntax, statement, statement);
    }
    
    /**
     * Creates a clause.
     * If {@code request} is given, the statement can be executed once.
     * @param dbString
     * @param statement
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, StatementBuilder dbString, Builder statement) {
        this.newBuilder = statement != null ? () -> {close(); return statement;}
                : () -> { throw new UnsupportedOperationException("Can't execute clause"); };
        this.owner = dbString;
        this.syntax = syntax;
    }

    protected Builder request() {
        return newBuilder.get();
    }

    protected Syntax getSyntax() {
        return syntax;
    }
    
    protected QlBuilder<?> newQlBuilder(MiDBString coreBuilder) {
        return new SyntaxStringBuilder(syntax, coreBuilder).begin(QlBuilder.TYPE);
    }
    
    protected void closeSubclauses() {
    }

    @Override
    public void close() {
        if (owner == null) {
            throw new UnsupportedOperationException("Only closable when used as clause");
        }
        if (closed) {
            throw new IllegalStateException("Already closed");
        }
        closed = true;
        closeSubclauses();
        buildStatement(owner);
    }
    
    @Override
    public String toString() {
        return _buildStatement(newToStringBuilder()).toString();
    }
    
    private <Stmt extends StatementBuilder> Stmt _buildStatement(Stmt stmt) {
        buildStatement(stmt);
        return stmt;
    }
    
    protected abstract StatementBuilder newToStringBuilder();
    
    protected abstract void buildStatement(StatementBuilder stmt);
    
    protected static <Builder> void append(Builder target, SubClause<? super Builder> part) {
        append(target, null, part, false);
    }
    
    protected static <Builder> void append(Builder target, SubClause<? super Builder> prefix, SubClause<? super Builder> part) {
        append(target, prefix, part, false);
    }
    
    protected static <Builder> void append(Builder target, SubClause<? super Builder> prefix, SubClause<? super Builder> part, boolean required) {
        if (part == null) return;
        if (part.isEmpty()) {
            if (required) {
                throw new IllegalStateException(
                        "Empty " + prefix + " clause");
            }
            return;
        }
        if (prefix != null) prefix.addTo(target);;
        part.addTo(target);
    }
    
    protected interface SubClause<Builder> {
        default boolean isEmpty() {
            return false;
        }
        void addTo(Builder builder);
    }
}
