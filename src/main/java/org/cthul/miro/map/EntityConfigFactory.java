package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public interface EntityConfigFactory<Entity> {
    
    EntityConfiguration<Entity> getConfiguration(MiConnection cnn, Mapping<? extends Entity> mapping, List<String> fields);
}
