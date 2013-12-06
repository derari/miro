package org.cthul.miro.map;

import org.cthul.miro.graph.EntityGraphAdapter;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.template.QueryTemplateProvider;

public interface MappedTemplateProvider<Entity> extends QueryTemplateProvider {
    
    Mapping<Entity> getMapping();
    
    EntityGraphAdapter<Entity> getGraphAdapter();
    
    @Override
    MappedTemplate getTemplate(QueryType<?> queryType);
}
