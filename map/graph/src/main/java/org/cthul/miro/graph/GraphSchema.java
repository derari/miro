package org.cthul.miro.graph;

import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.miro.db.MiConnection;

/**
 * A template for graphs.
 */
public interface GraphSchema {
    
    Graph newGraph(MiConnection connection);
    
    Graph newFakeGraph(MiConnection connection);
    
    static GraphSchemaBuilder build() {
        return new GraphSchemaBuilder();
    }
}
