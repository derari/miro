package org.cthul.miro.map;

import org.cthul.miro.db.stmt.MiStatement;
import org.cthul.miro.result.Results;

/**
 *
 */
public interface MappedQuery<Entity> extends MiStatement<Results<Entity>> {
    
}
