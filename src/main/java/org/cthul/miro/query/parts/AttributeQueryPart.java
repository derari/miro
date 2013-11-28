package org.cthul.miro.query.parts;

/**
 * A query part that represents an attribute.
 */
public interface AttributeQueryPart extends QueryPart {
    
    /** e.g. {@code firstName } */
    String getAttributeKey();
    
    /** e.g. {@code `first_name` } */
    String getSqlName();
}
