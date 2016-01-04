package org.cthul.miro.db.impl;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class MiUpdateQlBuilder extends QlBuilderDelegator<MiUpdateQlBuilder> implements MiUpdateString {
    
    private final MiUpdateString updateString;
    private final QlBuilder<?> qlBuilder;

    private MiUpdateQlBuilder(MiUpdateString updateString, Syntax syntax) {
        super(syntax);
        this.updateString = updateString;
        this.qlBuilder = syntax.newClause(updateString, QlBuilder.CLAUSE);
    }

    @Override
    protected QlBuilder<?> getDelegatee() {
        return qlBuilder;
    }

    @Override
    public Long execute() throws MiException {
        closeNestedClause();
        return updateString.execute();
    }

    @Override
    public MiAction<Long> asAction() {
        closeNestedClause();
        return updateString.asAction();
    }
    
    public static MiUpdateQlBuilder create(MiConnection cnn) {
        return cnn.newStatement(TYPE);
    }
    
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final RequestType<MiUpdateQlBuilder> TYPE = new RequestType<MiUpdateQlBuilder>() {
        @Override
        public MiUpdateQlBuilder createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return new MiUpdateQlBuilder(cnn.newUpdate(), syntax);
        }
    };
}
