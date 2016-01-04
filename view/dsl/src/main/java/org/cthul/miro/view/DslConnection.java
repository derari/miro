package org.cthul.miro.view;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.view.impl.DslConnectionDelegator;

/**
 *
 */
public interface DslConnection extends MiConnection, ViewCRUD<Object, DslConnection.Select, Object, Object> {
    
    static DslConnection wrap(MiConnection cnn) {
        if (cnn instanceof DslConnection) {
            return (DslConnection) cnn;
        }
        return new DslConnectionDelegator(cnn);
    }
    
    public interface Select {
        <V> V from(ViewR<V> view);
    }
}
