package org.cthul.miro.map;

import org.cthul.miro.dml.MappedCUD;
import org.cthul.miro.dml.MappedSelect;
import org.cthul.miro.result.Results;
import org.cthul.miro.view.ViewCRUD;

public interface MappedView<Entity> extends ViewCRUD<
                        MappedCUD<Entity>, 
                        MappedSelect<Entity, Results<Entity>>, 
                        MappedCUD<Entity>, 
                        MappedCUD<Entity>> {
    
}
