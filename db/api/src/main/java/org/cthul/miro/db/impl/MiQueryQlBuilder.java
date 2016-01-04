package org.cthul.miro.db.impl;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;

/**
 * A {@link QlBuilder} that can be executed and returns an {@link MiResultSet}.
 * Create instances using {@link #create(MiConnection)}.
 */
public class MiQueryQlBuilder extends QlBuilderDelegator<MiQueryQlBuilder> implements MiQueryString {
    
    private final MiQueryString queryString;
    private final QlBuilder<?> qlBuilder;

    /**
     * @param queryString   the string target that will run against the database
     * @param syntax        the syntax for building the query
     */
    private MiQueryQlBuilder(MiQueryString queryString, Syntax syntax) {
        super(syntax);
        this.queryString = queryString;
        this.qlBuilder = syntax.newClause(queryString, QlBuilder.CLAUSE);
    }

    @Override
    protected QlBuilder<?> getDelegatee() {
        return qlBuilder;
    }

    @Override
    public MiResultSet execute() throws MiException {
        closeNestedClause();
        return queryString.execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        closeNestedClause();
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
