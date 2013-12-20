package org.cthul.miro.result;

import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;

public interface QueryWithResult<Result> {
    
    Result execute(MiConnection cnn) throws SQLException;
    
    Result _execute(MiConnection cnn);
    
    MiFuture<Result> submit(MiConnection cnn);
}
