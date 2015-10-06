package org.cthul.miro.composer;

import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.RequestType;

/**
 *
 */
public abstract class StatementComposer<Statement, Builder extends StatementHolder<? extends Statement>>
                extends AbstractQueryComposer<Builder> {

    private final RequestType<Statement> requestType;
    private MiConnection connection;

    public StatementComposer(RequestType<Statement> requestType, Template<? super Builder> template) {
        super(template);
        this.requestType = requestType;
    }
    
    @Override
    public void put2(Object key, Object key2, Object... args) {
        if (key instanceof MiConnection) {
            connection = (MiConnection) key;
        }
        super.put2(key, key2, args);
    }

    protected RequestType<Statement> getRequestType() {
        return requestType;
    }

    protected MiConnection getConnection() {
        return connection;
    }
    
    protected Builder buildStatement() {
        Statement stmt = newStatement();
        Builder builder = newBuilder(stmt);
        buildStatement(builder);
        return builder;
    }
    
    protected Statement newStatement() {
        return connection.newStatement(requestType);
    }
    
    protected abstract Builder newBuilder(Statement statement);
}
