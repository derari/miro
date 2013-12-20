package org.cthul.miro.graph;

import org.cthul.miro.map.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public interface GraphConfigurationProvider<Entity> {
    
    <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping, Graph graph, Object[] args);
}
