package org.cthul.miro.sql.composer.model;

import java.util.function.BiConsumer;

/**
 *
 */
public interface JoinedView {
    
    String getPrefix();
    
    VirtualView newVirtualView();
    
    void collectJoinedViews(BiConsumer<String, JoinedView> bag);
    
    // TODO:
    // List<String> getKeyAttributes();
}
