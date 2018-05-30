package org.cthul.miro.sql.composer.model;

/**
 *
 */
public interface JoinedView {
    
    String getPrefix();
    
    VirtualView newVirtualView();
    
    // TODO:
    // List<String> getKeyAttributes();
}
