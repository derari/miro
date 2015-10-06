package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.util.Key;

/**
 * Defines database requests.
 * @param <Request>
 */
public interface RequestType<Request> extends Key<Request> {
    
    default Request createDefaultRequest(Syntax syntax, MiConnection cnn) {
        throw new UnsupportedOperationException(
                syntax + ": Unsupported request type " + this);
    }
}
