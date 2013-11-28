package org.cthul.miro.map;

import org.cthul.miro.MiConnection;
import org.cthul.miro.query.ZQueryBuilder;
import org.cthul.miro.query.ZQueryTemplate;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityInitializer;

/**
 *
 */
public class MappedQueryTemplate<Entity> extends ZQueryTemplate {
    
    private final SimpleMapping<Entity> mapping;

    public MappedQueryTemplate(SimpleMapping<Entity> mapping) {
        this.mapping = mapping;
    }

    public MappedQueryTemplate() {
        this.mapping = null;
    }
    
    public SimpleMapping<Entity> getMapping() {
        return mapping;
    }

    protected PartTemplate configure(String[] required, String key, Include include, ConfigurationProvider<? super Entity> factory) {
        return addPart(new ConfigPartTemplate<>(key, include, required, factory));
    }

    @Override
    protected Using<?> always() {
        return (Using) super.always();
    }

    @Override
    protected Using<?> always(String... keys) {
        return (Using) super.always(keys);
    }

    @Override
    protected Using<?> using() {
        return (Using) super.using();
    }

    @Override
    protected Using<?> using(String... keys) {
        return (Using) super.using(keys);
    }

    @Override
    protected Using<?> using(Include include, String... keys) {
        return new Using<>(include, keys);
    }
    
    public class Using<This extends Using> extends ZQueryTemplate.Using<This> {

        public Using(Include include, String[] required) {
            super(include, required);
        }
        
        public This configure(String key, ConfigurationProvider<? super Entity> factory) {
            MappedQueryTemplate.this.configure(required, key, include, factory);
            return _this();
        }
        
        public This configure(String key, EntityConfiguration<? super Entity> cfg) {
            return configure(key, ConfigurationInstance.asFactory(cfg));
        }
        
        public This configure(String key, EntityInitializer<? super Entity> init) {
            return configure(key, ConfigurationInstance.asFactory(init));
        }
    }
    
    protected static class ConfigPartTemplate<Entity> extends PartTemplate {
        private final ConfigurationProvider<Entity> factory;

        public ConfigPartTemplate(String key, Include include, String[] required, ConfigurationProvider<Entity> factory) {
            super(key, include, required);
            this.factory = factory;
        }

        @Override
        public ZQueryBuilder.QueryPart createPart(String alias) {
            return new ConfigQueryPart(alias, factory);
        }
    }
    
    protected static class ConfigQueryPart<Entity> extends ZQueryBuilder.CustomPart implements ConfigurationProvider<Entity> {
        private final ConfigurationProvider<Entity> factory;

        public ConfigQueryPart(String key, ConfigurationProvider<Entity> factory) {
            super(key, ZQueryBuilder.PartType.OTHER, null);
            this.factory = factory;
        }

        @Override
        public <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, SimpleMapping<E> mapping) {
            return factory.getConfiguration(cnn, mapping);
        }   
    }
}
