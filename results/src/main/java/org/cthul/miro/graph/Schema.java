package org.cthul.miro.graph;

import org.cthul.miro.db.MiConnection;

/**
 *
 */
public interface Schema {
    
    Graph newGraph(MiConnection connection);
    
    static SchemaBuilder build() {
        return new SchemaBuilder();
    }
}
