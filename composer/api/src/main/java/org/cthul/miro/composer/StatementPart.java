package org.cthul.miro.composer;

/**
 * Part of a composed statement.
 * @param <Builder>
 */
public interface StatementPart<Builder> {
    
    /**
     * Applies itself to the statement builder.
     * @param builder 
     */
    void addTo(Builder builder);
}
