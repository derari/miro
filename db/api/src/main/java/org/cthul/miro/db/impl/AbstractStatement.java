package org.cthul.miro.db.impl;

import java.util.function.Supplier;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.AutocloseableBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A complex statement consisting of several sub-clauses.
 * Can be used as a request or a clause.
 * @param <Statement>
 */
public abstract class AbstractStatement<Statement extends MiDBString> implements AutocloseableBuilder {
    
    private final Supplier<Statement> stmtAccess;
    private final MiDBString dbString;
    private final Syntax syntax;
    private boolean closed = false;

    /**
     * Creates a statement that can be executed multiple times.
     * @param stmtFactory
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Supplier<Statement> stmtFactory) {
        this.stmtAccess = () -> {
            closeSubclauses();
            Statement r = stmtFactory.get();
            return _buildStatement(r);
        };
        this.dbString = null;
        this.syntax = syntax;
    }

    /**
     * Creates statement as a request-clause that can be executed only once.
     * @param statement
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Statement statement) {
        this(syntax, statement, statement);
    }
    
    /**
     * Creates a clause.
     * If {@code request} is given, the statement can be executed once.
     * @param dbString
     * @param statement
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, MiDBString dbString, Statement statement) {
        this.stmtAccess = statement != null ? () -> statement 
                : () -> { throw new UnsupportedOperationException("Can't execute clause"); };
        this.dbString = dbString;
        this.syntax = syntax;
    }

    protected Statement request() {
        return stmtAccess.get();
    }

    protected Syntax getSyntax() {
        return syntax;
    }
    
    protected QlBuilder<?> newQlBuilder(MiDBString coreBuilder) {
        return QlBuilder.create(getSyntax(), coreBuilder);
    }
    
    protected void closeSubclauses() {
    }

    @Override
    public void close() {
        if (dbString == null) {
            throw new UnsupportedOperationException("Only closable when used as clause");
        }
        if (closed) {
            throw new IllegalStateException("Already closed");
        }
        closed = true;
        closeSubclauses();
        buildStatement(dbString);
    }
    
    @Override
    public String toString() {
        return _buildStatement(new MiDBStringBuilder()).toString();
    }
    
    private <Stmt extends MiDBString> Stmt _buildStatement(Stmt stmt) {
        buildStatement(stmt);
        return stmt;
    }
    
    protected abstract void buildStatement(MiDBString stmt);
    
    protected void append(MiDBString target, SubClause part) {
        append(target, null, part, false);
    }
    
    protected void append(MiDBString target, String prefix, SubClause part) {
        append(target, prefix, part, false);
    }
    
    protected void append(MiDBString target, String prefix, SubClause part, boolean required) {
        if (part == null) return;
        if (part.isEmpty()) {
            if (required) {
                throw new IllegalStateException(
                        "Empty " + prefix.trim() + " clause");
            }
            return;
        }
        if (prefix != null) target.append(prefix);
        part.addTo(target);
    }
    
    protected interface SubClause {
        boolean isEmpty();
        void addTo(MiDBString dbString);
    }
}
