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

    protected PartTemplate configure(String[] required, String key, Include include, ConfigurationProvider<Entity> factory) {
        return addPart(new ConfigPartTemplate(key, include, required, factory));
    }
    
    protected static class ConfigPartTemplate<Entity> extends PartTemplate {
        private final ConfigurationProvider<Entity> factory;

        public ConfigPartTemplate(String key, Include include, String[] required, ConfigurationProvider<Entity> factory) {
            super(key, include, required);
            this.factory = factory;
        }

        @Override
        public QueryBuilder.QueryPart createPart(String alias) {
            return new ConfigQueryPart(alias, factory);
        }
    }
    
    protected static class ConfigQueryPart<Entity> extends QueryBuilder.CustomPart implements ConfigurationProvider<Entity> {
        private final ConfigurationProvider<Entity> factory;

        public ConfigQueryPart(String key, ConfigurationProvider<Entity> factory) {
            super(key, QueryBuilder.PartType.OTHER, null);
            this.factory = factory;
        }

        @Override
        public <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping) {
            return factory.getConfiguration(cnn, mapping);
        }   
    }
}
