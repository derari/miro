package org.cthul.miro.ext.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.*;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.string.MiDBStringBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.string.NestableStatementBuilder;

/**
 *
 */
public abstract class JdbcStatement extends NestableStatementBuilder {
    
    protected final JdbcConnection connection;
    private final MiDBStringBuilder coreBuilder = new MiDBStringBuilder();
    private List<List<Object>> batches;

    public JdbcStatement(JdbcConnection connection) {
        this.connection = connection;
    }

    @Override
    protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
        if (type == MiDBString.TYPE) {
            return type.cast(coreBuilder);
        }
        return connection.getSyntax().newClause(parent, type);
    }

    @Override
    public String toString() {
        return coreBuilder.toString();
    }

    protected List<Object> getArguments() {
        return coreBuilder.getArguments();
    }
    
    protected void addBatch() {
        if (batches == null) {
            batches = new ArrayList<>();
        }
        batches.add(new ArrayList<>(getArguments()));
        getArguments().clear();
    }
    
    protected PreparedStatement preparedStatement() throws MiException {
        closeNestedClause();
        PreparedStatement stmt = connection.prepareStatement(toString());
        if (batches != null) {
            if (!getArguments().isEmpty()) addBatch();
            try {
                for (List<Object> batch: batches) {
                    setArguments(stmt, batch);
                    stmt.addBatch();
                }
            } catch (SQLException e) {
                throw new MiException(e);
            }
        } else {
            setArguments(stmt, getArguments());
        }
        return stmt;
    }
    
    protected void setArguments(PreparedStatement stmt, List<Object> arguments) throws MiException {
        for (int i = 0; i < arguments.size(); i++) {
            try {
                stmt.setObject(i+1, arguments.get(i));
            } catch (SQLException e) {
                throw new MiException(e.getMessage() + "\n"
                        + stmt.toString() + "\n"
                        + (i+1) + " = " + arguments.get(i), e);
            }
        }
    }
    
    protected <R> R withRetry(StmtExecutor<R> exec) throws MiException {
        PreparedStatement ps = preparedStatement();
        try {
            return exec.apply(ps);
        } catch (MiException e) {
            try {
                if (retry(ps)) {
                    return exec.apply(ps);
                }
            } catch (MiException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }
    
    protected boolean retry(PreparedStatement ps) {
        try {
            Connection c = ps.getConnection();
            if (c.isValid(0)) return false;
            else c.close();
        } catch (SQLException e) { }
        try {
            Connection c = connection.getConnection();
            return c.isValid(0);
        } catch (SQLException e) {
            return false;
        }
    }
    
    protected static interface StmtExecutor<R> {
        
        R apply(PreparedStatement stmt) throws MiException;
    }
}
