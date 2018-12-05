package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.util.Key;
import org.cthul.miro.db.request.MiRequest;

/**
 * Defines database requests.
 * @param <Req>
 */
public interface RequestType<Req extends MiRequest<?>> extends Key<Req> {
    
    default Req newRequest(MiConnection cnn) {
        // connection provides actual implementation,
        // calls createDefautRequest as fallback
        return cnn.newRequest(this);
    }
    
    default Req createDefaultRequest(Syntax syntax, MiConnection cnn) {
        throw new UnsupportedOperationException(
                syntax + ": Unsupported request type " + this);
    }
}
