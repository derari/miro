package org.cthul.miro.set;

import java.util.List;
import org.cthul.miro.result.Results;

public interface ReadSet<Entity> {
    
    Results.Action<Entity> action();
    
    default List<Entity> getList() {
        return action()._asList();
    }
}
