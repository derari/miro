package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class AbstractNestedBuilder<Owner, This extends QlBuilder<This>> 
                extends QlBuilderDelegator<This>
                implements NestedBuilder<Owner> {

    private final Owner owner;
    private final QlBuilder<?> builder;
    private boolean initialized;

    public AbstractNestedBuilder(Owner owner, MiDBString dbString, Syntax syntax) {
        this(owner, syntax.newClause(dbString, QlBuilder.CLAUSE), syntax);
    }

    public AbstractNestedBuilder(Owner owner, QlBuilder<?> builder, Syntax syntax) {
        super(syntax);
        this.owner = owner;
        this.builder = builder;
    }

    private void makeInitialized() {
        if (!initialized) {
            initialized = true;
            open();
        }
    }
    
    protected void open() { }
    
    @Override
    protected QlBuilder<?> getDelegatee() {
        makeInitialized();
        return builder;
    }
    
    @Override
    public Owner end() {
        makeInitialized();
        close();
        return owner;
    }
}
