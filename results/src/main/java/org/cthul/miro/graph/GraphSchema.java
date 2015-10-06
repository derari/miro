package org.cthul.miro.graph;

import org.cthul.miro.db.MiConnection;

/**
 *
 */
public interface GraphSchema {
    
    Graph newGraph(MiConnection connection);
    
    static GraphSchemaBuilder build() {
        return new GraphSchemaBuilder();
    }
}
