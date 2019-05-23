package org.cthul.miro.ext.jdbc;

import org.cthul.miro.db.*;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiUpdateBuilder;

/**
 *
 */
public class JdbcUpdate extends JdbcStatement implements MiUpdateBuilder {

    public JdbcUpdate(JdbcConnection connection) {
        super(connection);
    }

    @Override
    public Long execute() throws MiException {
        return withRetry(connection::executeStatement);
    }

    @Override
    public MiAction<Long> asAction() {
        return connection
                .stmtAction(this::preparedStatement);
    }

    @Override
    public void addBatch() {
        super.addBatch();
    }
}
