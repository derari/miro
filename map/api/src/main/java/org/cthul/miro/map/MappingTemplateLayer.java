package org.cthul.miro.map;

import org.cthul.miro.view.composer.SimpleCRUDTemplateLayer;

/**
 *
 * @param <Entity>
 */
public interface MappingTemplateLayer<Entity, This extends MappingTemplateLayer<Entity, This>>
                 extends MappingBuilder<Entity, This> {
    
    <CS, RS, US, DS> SimpleCRUDTemplateLayer<MappedStatementBuilder<Entity, ? extends CS>, 
                            MappedStatementBuilder<Entity, ? extends RS>,
                            MappedStatementBuilder<Entity, ? extends US>,
                            MappedStatementBuilder<Entity, ? extends DS>> asLayer();
}
