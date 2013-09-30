package org.cthul.miro.at;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.map.*;
import org.cthul.miro.result.ResultBuilder;

/**
 * Provides methods of {@link MappedStatement}
 */
public interface AnnotatedStatement<Entity> {
    
    ResultSet runQuery() throws SQLException;

    ResultSet runQuery(MiConnection cnn) throws SQLException;

    MiFuture<ResultSet> submitQuery() throws SQLException;

    MiFuture<ResultSet> submitQuery(MiConnection cnn) throws SQLException;

    <R> SubmittableQuery<R> as(ResultBuilder<R, Entity> rb);

    SubmittableQuery<Entity[]> asArray();

    SubmittableQuery<List<Entity>> asList();

    SubmittableQuery<ResultCursor<Entity>> asCursor();

    SubmittableQuery<Entity> getSingle();

    SubmittableQuery<Entity> getFirst();
}
