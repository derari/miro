package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.syntax.RequestString;
import org.cthul.miro.db.syntax.RequestBuilderDelegator;
import org.cthul.miro.db.syntax.RequestBuilder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.db.*;

/**
 *
 */
public abstract class JdbcRequest<This extends RequestBuilder<This>> extends RequestBuilderDelegator<This> {
    
    protected final JdbcConnection connection;
    private final RequestString queryString;

    public JdbcRequest(JdbcConnection connection, RequestString queryString) {
        this.connection = connection;
        this.queryString = queryString;
    }

    @Override
    protected RequestBuilder<?> getDelegatee() {
        return queryString;
    }
    
    protected List<Object> getArguments() {
        return queryString.getArguments();
    }
    
    protected PreparedStatement preparedStatement() throws MiException {
        PreparedStatement stmt = connection.prepareStatement(toString());
        List<Object> arguments = getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            try {
                stmt.setObject(i+1, arguments.get(i));
            } catch (SQLException e) {
                throw new MiException(e);
            }
        }
        return stmt;
    }

}
