package org.cthul.miro.db.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.db.syntax.BasicCoreStmtBuilder;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.CoreStmtBuilderDelegator;

/**
 *
 */
public abstract class JdbcStatement<This extends CoreStmtBuilder> extends CoreStmtBuilderDelegator<This> {
    
    protected final JdbcConnection connection;
    private final BasicCoreStmtBuilder coreBuilder = new BasicCoreStmtBuilder();

    public JdbcStatement(JdbcConnection connection) {
        this.connection = connection;
    }

    @Override
    protected CoreStmtBuilder getDelegatee() {
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
                throw new MiException(e);
            }
        }
        return stmt;
    }
}
