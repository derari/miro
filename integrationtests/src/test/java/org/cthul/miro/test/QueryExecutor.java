package org.cthul.miro.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.jdbc.JdbcResultSet;

/**
 *
 */
public class QueryExecutor {
    
    public static MiFuture<MiResultSet> submit(MiFunction<Connection, PreparedStatement> query) {
        Connection cnn = TestDB.getConnection();
        return query.asAction(cnn).andThen(ps -> {
            ResultSet rs = ps.executeQuery();
            ps.closeOnCompletion();
            return (MiResultSet) new JdbcResultSet(rs);
        }).getTrigger();
    }
    
}
