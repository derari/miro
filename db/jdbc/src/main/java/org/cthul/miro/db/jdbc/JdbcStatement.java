package org.cthul.miro.db.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.db.impl.BasicDBStringBuilder;
import org.cthul.miro.db.impl.MiDBStringDelegator;
import org.cthul.miro.db.stmt.MiDBString;

/**
 *
 * @param <This>
 */
public abstract class JdbcStatement<This extends MiDBString> extends MiDBStringDelegator<This> {
    
    protected final JdbcConnection connection;
    private final BasicDBStringBuilder coreBuilder = new BasicDBStringBuilder();

    public JdbcStatement(JdbcConnection connection) {
        this.connection = connection;
    }

    @Override
    protected BasicDBStringBuilder getDelegatee() {
        return coreBuilder;
    }

    protected List<Object> getArguments() {
        return coreBuilder.getArguments();
    }
    
    protected PreparedStatement preparedStatement() throws MiException {
        PreparedStatement stmt = connection.prepareStatement(toString());
        List<Object> arguments = getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            try {
                stmt.setObject(i+1, arguments.get(i));
            } catch (SQLException e) {
                throw new MiException(e.getMessage() + "\n"
                        + stmt.toString() + "\n"
                        + (i+1) + " = " + arguments.get(i), e);
            }
        }
        return stmt;
    }
}
