package org.cthul.miro.map;

import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public interface ConfigurationProvider<Entity> {
    
    <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping);
}
