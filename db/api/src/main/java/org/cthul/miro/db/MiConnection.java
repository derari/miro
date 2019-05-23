package org.cthul.miro.db;

import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.db.request.MiUpdateBuilder;

/**
 * Represents a database connection.
 * Can create statements in the database's syntax flavor.
 */
public interface MiConnection extends AutoCloseable {
    
    MiQueryBuilder newQuery();
    
    MiUpdateBuilder newUpdate();
    
    <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type);

    @Override
    void close() throws MiException;
}
