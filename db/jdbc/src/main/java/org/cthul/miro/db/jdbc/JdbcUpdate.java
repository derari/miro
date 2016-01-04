package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.*;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class JdbcUpdate extends JdbcStatement implements MiUpdateString {

    public JdbcUpdate(JdbcConnection connection) {
        super(connection);
    }

    @Override
    public Long execute() throws MiException {
        return connection.executeStatement(preparedStatement());
    }

    @Override
    public MiAction<Long> asAction() {
        return connection
                .stmtAction(this::preparedStatement);
    }
}
