package org.cthul.miro.entity.map;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;

/**
 *
 */
public interface ColumnMapping<Cnn> {
    
    List<String> getColumns();
    
    Object[] toColumns(Object value, Object[] result);
    
    boolean accept(MiResultSet rs, Cnn cnn) throws MiException;

    EntityFactory<?> newValueReader(MiResultSet rs, Cnn cnn) throws MiException;
}
