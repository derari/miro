package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.syntax.RequestString;
import org.cthul.miro.db.syntax.MiQueryString;
import java.sql.ResultSet;
import org.cthul.miro.db.*;
import org.cthul.miro.db.syntax.MiStatementString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class JdbcStatement extends JdbcRequest<MiStatementString> implements MiStatementString {
    
    public JdbcStatement(JdbcConnection connection, RequestString queryString) {
        super(connection, queryString);
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
