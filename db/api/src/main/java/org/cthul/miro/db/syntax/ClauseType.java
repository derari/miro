package org.cthul.miro.db.syntax;

import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.util.Key;

/**
 * Defines a statement clause.
 * @param <Clause>
 */
public interface ClauseType<Clause> extends Key<Clause> {

    default Clause createDefaultClause(Syntax syntax, MiDBString dbString, Object owner) {
        throw new UnsupportedOperationException(
                syntax + ": Unsupported clause type " + this + " for " + 
                        (owner != null ? owner : dbString));
    }
}
