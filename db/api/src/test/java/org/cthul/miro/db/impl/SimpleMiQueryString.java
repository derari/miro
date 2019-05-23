package org.cthul.miro.db.impl;

import org.cthul.miro.db.syntax.QlBuilderDelegator;
import java.util.function.Consumer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiQueryBuilder;

/**
 * A {@link QlBuilder} that can be executed and returns an {@link MiResultSet}.
 * Create instances using {@link #create(MiConnection)}.
 */
public class SimpleMiQueryString extends QlBuilderDelegator<SimpleMiQueryString> implements MiQueryBuilder {

    private final MiQueryBuilder queryString;
    private final QlBuilder<?> qlBuilder;

    /**
     * @param queryString   the string target that will run against the database
     * @param syntax        the syntax for building the query
     */
    SimpleMiQueryString(MiQueryBuilder queryString, Syntax syntax) {
        super(syntax);
        this.queryString = queryString;
        this.qlBuilder = syntax.newClause(queryString, QlBuilder.TYPE);
    }

    @Override
    protected QlBuilder<?> getDelegate() {
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

    @Override
    public <Clause> SimpleMiQueryString clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        return super.clause(type, code);
    }
}
