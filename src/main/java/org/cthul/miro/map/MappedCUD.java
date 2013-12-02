package org.cthul.miro.map;

import org.cthul.miro.query.api.QueryType;

public class MappedCUD<Entity> extends AbstractMappedQuery<Entity> {
    
    private final String prefix;

    public MappedCUD(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider, String[] select) {
        super(type, templateProvider);
        prefix = type.toString().toLowerCase() + "-";
        putAll(select);
    }
    
    public MappedCUD values(Entity... args) {
        put(prefix  + "entities.addAll", args);
        return this;
    }
}
