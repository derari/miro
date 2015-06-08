package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.syntax.RequestString;
import org.cthul.miro.db.syntax.RequestBuilderDelegator;
import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.syntax.RequestBuilder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class JdbcQuery extends JdbcRequest<MiQueryString> implements MiQueryString {
    
    public JdbcQuery(JdbcConnection connection, RequestString queryString) {
        super(connection, queryString);
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
