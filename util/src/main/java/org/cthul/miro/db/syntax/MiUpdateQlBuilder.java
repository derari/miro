package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class MiUpdateQlBuilder extends QlBuilderDelegator<MiUpdateQlBuilder> implements MiUpdateString {
    
    private final MiUpdateString updateString;
    private final QlBuilder<?> qlBuilder;

    public MiUpdateQlBuilder(MiUpdateString updateString, Syntax syntax) {
        super(syntax);
        this.updateString = updateString;
        this.qlBuilder = syntax.newClause(updateString, QlBuilder.TYPE);
    }

    @Override
    protected QlBuilder<?> getDelegatee() {
        return qlBuilder;
    }

    @Override
    public Long execute() throws MiException {
        return updateString.execute();
    }

    @Override
    public MiAction<Long> asAction() {
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
