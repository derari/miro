package org.cthul.miro.composer.impl;

import org.cthul.miro.composer.Template;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.RequestType;

/**
 * Composer implementation that expects an {@link MiConnection}.
 * @param <Statement>
 * @param <Builder>
 */
public abstract class AbstractRequestComposer<Statement, Builder>
                extends AbstractComposer<Builder> {

    private final RequestType<Statement> requestType;
    private MiConnection connection;

    public AbstractRequestComposer(RequestType<Statement> requestType, Template<? super Builder> template) {
        super(template);
        if (requestType == null) throw new NullPointerException("requestType");
        this.requestType = requestType;
    }

    @Override
    public void require(Object key) {
        if (key instanceof MiConnection) {
            setConnection((MiConnection) key);
        }
        super.require(key);
    }
    
    protected RequestType<Statement> getRequestType() {
        return requestType;
    }

    public void setConnection(MiConnection connection) {
        this.connection = connection;
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
