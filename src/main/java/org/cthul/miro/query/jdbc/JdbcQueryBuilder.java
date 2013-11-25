package org.cthul.miro.query.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.query.api.QueryAdapter;

public interface JdbcQueryBuilder extends QueryAdapter {
    
    ResultSet execute(Connection connection) throws SQLException;
}
