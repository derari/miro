package org.cthul.miro.map;

import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.query.QueryTemplate;
import org.cthul.miro.result.EntitySetup;

/**
 *
 */
public class MappedTemplate<Entity> extends QueryTemplate {
    
    private final Mapping<Entity> mapping;

    public MappedTemplate(Mapping<Entity> mapping) {
        this.mapping = mapping;
    }

    public MappedTemplate() {
        this.mapping = null;
    }
    
    public Mapping<Entity> getMapping() {
        return mapping;
    }

    protected void setup(String[] required, String key, Include include, EntitySetupFactory<Entity> factory) {
        addPart(new SetupPartTemplate(key, include, required, factory));
    }
    
    protected static class SetupPartTemplate<Entity> extends PartTemplate<Entity> {
        private final EntitySetupFactory<Entity> factory;

        public SetupPartTemplate(String key, Include include, String[] required, EntitySetupFactory<Entity> factory) {
            super(key, include, required);
            this.factory = factory;
        }

        @Override
        public QueryBuilder.QueryPart createPart(String alias) {
            return new SetupQueryPart(alias, factory);
        }
    }
    
    protected static class SetupQueryPart<Entity> extends QueryBuilder.CustomPart implements MappedStatement.SetupProvider<Entity> {
        private final EntitySetupFactory<Entity> factory;

        public SetupQueryPart(String key, EntitySetupFactory<Entity> factory) {
            super(key, QueryBuilder.PartType.OTHER, null);
            this.factory = factory;
        }

        @Override
        public EntitySetup<Entity> getSetup(MiConnection cnn, Mapping<? extends Entity> mapping) {
            return factory.getSetup(cnn, mapping, null);
        }   
    }
}
