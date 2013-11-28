package org.cthul.miro.query.adapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcQuery<Builder extends QueryBuilder<? extends Builder>> extends QueryAdapter<Builder> {
    
    ResultSet execute(Connection connection) throws SQLException;
}
