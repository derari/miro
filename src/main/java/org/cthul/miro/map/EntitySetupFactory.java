package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntitySetup;

/**
 *
 */
public interface EntitySetupFactory<Entity> {
    
    EntitySetup<Entity> getSetup(MiConnection cnn, Mapping<? extends Entity> mapping, List<String> fields);
}
