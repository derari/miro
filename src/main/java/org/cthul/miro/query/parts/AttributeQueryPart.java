package org.cthul.miro.query.parts;

import org.cthul.miro.query.api.QueryPart;

/**
 * A query part that represents an attribute.
 * <p>
 * sql: {@code a = ?}
 */
public interface AttributeQueryPart extends QueryPart {
    
    String getAttribute();
}
