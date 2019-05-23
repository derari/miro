package org.cthul.miro.ext.jdbc;

import java.sql.ResultSet;
import org.cthul.miro.db.*;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiQueryBuilder;

/**
 *
 */
public class JdbcQuery extends JdbcStatement implements MiQueryBuilder {

    public JdbcQuery(JdbcConnection connection) {
        super(connection);
    }

    @Override
    public MiResultSet execute() throws MiException {
        return resultSet(withRetry(connection::executeQuery));
    }
    
    protected MiResultSet resultSet(ResultSet rs) {
        return new JdbcResultSet(rs);
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return connection
                .queryAction(this::preparedStatement)
                .andThen(this::resultSet);
    }
}
