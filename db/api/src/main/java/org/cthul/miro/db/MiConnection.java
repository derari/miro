package org.cthul.miro.db;

import org.cthul.miro.db.request.MiQueryString;
import org.cthul.miro.db.request.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.request.MiRequest;

/**
 * Represents a database connection.
 * Can create statements in the database's syntax flavor.
 */
public interface MiConnection extends AutoCloseable {
    
    MiQueryString newQuery();
    
    MiUpdateString newUpdate();
    
    <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type);

    @Override
    void close() throws MiException;
}
