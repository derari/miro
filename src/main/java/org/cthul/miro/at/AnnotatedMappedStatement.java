package org.cthul.miro.at;

import org.cthul.miro.map.z.SubmittableQuery;
import java.util.List;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.map.*;
import org.cthul.miro.result.ResultBuilder;

/**
 * Provides methods of {@link MappedStatement}
 */
public interface AnnotatedMappedStatement<Entity> {
    
//    ResultSet runQuery() throws SQLException;
//
//    ResultSet runQuery(MiConnection cnn) throws SQLException;
//
//    MiFuture<ResultSet> submitQuery() throws SQLException;
//
//    MiFuture<ResultSet> submitQuery(MiConnection cnn) throws SQLException;

    <R> SubmittableQuery<R> as(ResultBuilder<R, Entity> rb);

    SubmittableQuery<Entity[]> asArray();

    SubmittableQuery<List<Entity>> asList();

    SubmittableQuery<ResultCursor<Entity>> asCursor();

    SubmittableQuery<Entity> getSingle();

    SubmittableQuery<Entity> getFirst();
}
