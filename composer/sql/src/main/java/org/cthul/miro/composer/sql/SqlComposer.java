package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.AbstractRequestComposer;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.syntax.RequestType;

/**
 *
 * @param <Statement>
 */
public class SqlComposer<Statement> extends AbstractRequestComposer<Statement, Statement> {

    public SqlComposer(RequestType<Statement> requestType, Template<? super Statement> template) {
        super(requestType, template);
    }

    @Override
    protected Statement newBuilder(Statement statement) {
        return statement;
    }

    @Override
    public Statement buildStatement() {
        return super.buildStatement();
    }
    
    public static SqlComposer<SelectQuery> select(Template<? super SelectQuery> template) {
        return new SqlComposer<>(SqlDQML.SELECT, template);
    }
}
