package org.cthul.miro.entity.map;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;

/**
 *
 */
public interface ColumnValue {
    
    List<String> getColumns();
    
    Object[] toColumns(Object value, Object[] result);

    EntityFactory<?> newValueReader(MiResultSet rs) throws MiException;    
}
