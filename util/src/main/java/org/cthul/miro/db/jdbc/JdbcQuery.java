package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.syntax.MiQueryString;
import java.sql.ResultSet;
import org.cthul.miro.db.*;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class JdbcQuery extends JdbcStatement<JdbcQuery> implements MiQueryString {

    public JdbcQuery(JdbcConnection connection) {
        super(connection);
    }

    @Override
    public MiResultSet execute() throws MiException {
        return resultSet(connection.executeQuery(preparedStatement()));
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
