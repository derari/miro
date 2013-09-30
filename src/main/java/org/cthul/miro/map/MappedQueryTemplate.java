package org.cthul.miro.map;

import org.cthul.miro.MiConnection;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.query.QueryTemplate;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public class MappedQueryTemplate<Entity> extends QueryTemplate {
    
    private final Mapping<Entity> mapping;

    public MappedQueryTemplate(Mapping<Entity> mapping) {
        this.mapping = mapping;
    }

    public MappedQueryTemplate() {
        this.mapping = null;
    }
    
    public Mapping<Entity> getMapping() {
        return mapping;
    }

    protected void setup(String[] required, String key, Include include, EntityConfigFactory<Entity> factory) {
        addPart(new SetupPartTemplate(key, include, required, factory));
    }
    
    protected static class SetupPartTemplate<Entity> extends PartTemplate {
        private final EntityConfigFactory<Entity> factory;

        public SetupPartTemplate(String key, Include include, String[] required, EntityConfigFactory<Entity> factory) {
            super(key, include, required);
            this.factory = factory;
        }

        @Override
        public QueryBuilder.QueryPart createPart(String alias) {
            return new SetupQueryPart(alias, factory);
        }
    }
    
    protected static class SetupQueryPart<Entity> extends QueryBuilder.CustomPart implements MappedStatement.ConfigurationPart<Entity> {
        private final EntityConfigFactory<Entity> factory;

        public SetupQueryPart(String key, EntityConfigFactory<Entity> factory) {
            super(key, QueryBuilder.PartType.OTHER, null);
            this.factory = factory;
        }

        @Override
        public EntityConfiguration<Entity> getConfiguration(MiConnection cnn, Mapping<? extends Entity> mapping) {
            return factory.getConfiguration(cnn, mapping, null);
        }   
    }
}
