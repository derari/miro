package org.cthul.miro.sql.composer;

import org.cthul.miro.composer.node.MapNode;
import org.cthul.miro.sql.composer.model.VirtualView;

/**
 *
 */
public interface SqlDqmlComposer {
    
    VirtualView getMainView();
    
    MapNode<String, VirtualView> getViews();
}
