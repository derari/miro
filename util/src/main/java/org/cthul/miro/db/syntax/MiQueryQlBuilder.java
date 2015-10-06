package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class MiQueryQlBuilder extends QlBuilderDelegator<MiQueryQlBuilder> implements MiQueryString {
    
    private final MiQueryString queryString;
    private final QlBuilder<?> qlBuilder;

    public MiQueryQlBuilder(MiQueryString queryString, Syntax syntax) {
        super(syntax);
        this.queryString = queryString;
        this.qlBuilder = syntax.newClause(queryString, QlBuilder.TYPE);
    }

    @Override
    protected QlBuilder<?> getDelegatee() {
        return qlBuilder;
    }

    @Override
    public MiResultSet execute() throws MiException {
        return queryString.execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return queryString.asAction();
    }
    
    public static MiQueryQlBuilder create(MiConnection cnn) {
        return cnn.newStatement(TYPE);
    }
    
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final RequestType<MiQueryQlBuilder> TYPE = new RequestType<MiQueryQlBuilder>() {
        @Override
        public MiQueryQlBuilder createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return new MiQueryQlBuilder(cnn.newQuery(), syntax);
        }
    };
}
