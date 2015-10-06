package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;

/**
 *
 */
public interface Field<Entity> {

    EntityConfiguration<Entity> getResultReader();
}
