package org.cthul.miro.db.impl;

import java.util.function.Supplier;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.AutocloseableBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A complex statement consisting of several sub-clauses.
 * Can be used as a request or a clause.
 * @param <Request>
 */
public abstract class AbstractStatement<Request extends MiDBString> implements AutocloseableBuilder {
    
    private final Supplier<Request> requestAccess;
    private final MiDBString dbString;
    private final Syntax syntax;
    private boolean closed = false;

    /**
     * Creates a statement that can be executed multiple times.
     * @param requestFactory
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Supplier<Request> requestFactory) {
        this.requestAccess = () -> {
            closeSubclauses();
            Request r = requestFactory.get();
            return stmt(r);
        };
        this.dbString = null;
        this.syntax = syntax;
    }

    /**
     * Creates statement as a request-clause that can be executed only once.
     * @param request
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, Request request) {
        this(syntax, request, request);
    }
    
    /**
     * Creates a clause.
     * If {@code request} is given, the statement can be executed once.
     * @param dbString
     * @param request
     * @param syntax 
     */
    public AbstractStatement(Syntax syntax, MiDBString dbString, Request request) {
        this.requestAccess = request != null ? () -> request 
                : () -> { throw new UnsupportedOperationException("Can't execute clause"); };
        this.dbString = dbString;
        this.syntax = syntax;
    }

    protected Request request() {
        return requestAccess.get();
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
        return stmt(new MiDBStringBuilder()).toString();
    }
    
    protected <Stmt extends MiDBString> Stmt stmt(Stmt stmt) {
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
    
    protected void append(MiDBString target, String prefix, SubClause part, boolean forceNonEmpty) {
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
    
    protected interface SubClause {
        boolean isEmpty();
        void addTo(MiDBString dbString);
    }
}
