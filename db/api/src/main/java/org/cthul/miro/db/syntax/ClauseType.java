package org.cthul.miro.db.syntax;

import org.cthul.miro.util.Key;

/**
 * Defines a statement clause.
 * @param <Clause>
 */
public interface ClauseType<Clause> extends Key<Clause> {

    default Clause createDefaultClause(Syntax syntax, Object parent) {
        throw new UnsupportedOperationException(
                syntax + ": Unsupported clause type " + this + " for " + parent);
    }
}
