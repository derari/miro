package org.cthul.miro.result;

import java.util.List;
import org.cthul.miro.db.MiException;

/**
 *
 */
public interface EntityList<Entity> extends List<Entity>, AutoCloseable {

    @Override
    void close() throws MiException;
}
