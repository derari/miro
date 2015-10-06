package org.cthul.miro.db.syntax;

/**
 * Defines a statement clause.
 * @param <Clause>
 */
public interface ClauseType<Clause> {

    default Clause createDefaultClause(Syntax syntax, Object parent) {
        throw new UnsupportedOperationException(
                syntax + ": Unsupported clause type " + this + " for " + parent);
    }
    
    default Clause cast(Object o) {
        return (Clause) o;
    }
}
