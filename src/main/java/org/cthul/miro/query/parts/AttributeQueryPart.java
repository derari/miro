package org.cthul.miro.query.parts;

/**
 * A query part that represents an attribute.
 * <p>
 * sql: {@code a = ?}
 */
public interface AttributeQueryPart extends QueryPart {
    
    String getAttribute();
}
