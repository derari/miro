package org.cthul.miro.map.sql;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.impl.SimpleRequestComposer;
import org.cthul.miro.composer.sql.SqlAttribute;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.composer.template.TemplateLayerStack;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.impl.AbstractEntityNodeType;
import org.cthul.miro.util.Closables;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.composer.sql.SqlComposerKey;

/**
 *
 * @param <Entity>
 */
public class MappedEntityType<Entity> extends AbstractEntityNodeType<Entity> {
    
//    private final GraphSchema schema;
    private final Class<Entity> clazz;
    private boolean initialized = false;
    private final MyMapping mapping = new MyMapping();
    private Constructor<Entity> constructor = null;

    public MappedEntityType(Class<Entity> clazz) {
        this(null, clazz);
    }

    public MappedEntityType(GraphSchema schema, Class<Entity> clazz) {
        super(clazz.getSimpleName());
//        this.schema = schema;
        this.clazz = clazz;
    }
    
    public void initialize() {
        initialized = true;
    }

    public Class<Entity> getEntityClass() {
        return clazz;
    }
    
    protected void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized yet.");
        }
    }

    @Override
    public Entity[] newArray(int length) {
        return (Entity[]) Array.newInstance(clazz, length);
    }

    @Override
    protected Entity newEntity(Object[] key) {
        requireInitialized();
        final Entity e;
        try {
            if (constructor == null) {
                constructor = clazz.getConstructor();
            }
            e = constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw Closables.unchecked(ex);
        }
        if (key == null) return e;
        if (key.length != mapping.keys.size()) {
            throw new IllegalArgumentException(
                    "Expected " + mapping.keys.size() + " key values, "
                            + "got " + key.length);
        }
        for (int i = 0; i < key.length; i++) {
            mapping.getKeySetters().get(i).accept(e, key[i]);
        }
        return e;
    }

    @Override
    protected Object[] getKey(Entity e, Object[] array) {
        requireInitialized();
        if (array == null) array = new Object[mapping.keys.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = mapping.getKeyGetters().get(i).apply(e);
        }
        return array;
    }

    @Override
    protected KeyReader newKeyReader(MiResultSet resultSet) throws MiException {
        requireInitialized();
        return newKeyReader(resultSet, mapping.keys);
    }

    @Override
    protected EntityConfiguration<Entity> createAttributeReader(GraphApi graph, List<?> attributes) {
        return mapping.attributeConfiguration(flattenStr(attributes));
    }

    @Override
    protected BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
        return new AbstractBatchLoader() {
            @Override
            protected void fillAttributes(List<Object[]> keys) throws MiException {
                QueryableEntitySet<Entity> entitySet = new QueryableEntitySet<>(getType());
                entitySet.setConnection(graph);
                RequestComposer<MappedStatement<Entity, ? extends SelectBuilder>> cmp = new SimpleRequestComposer<>(getSelectLayer().build());
                cmp.requireAll(attributes, ComposerKey.FETCH_KEYS);
                cmp.node(SqlComposerKey.FIND_BY_KEYS).addAll(keys);
                try {
                    entitySet.query(SqlDQML.select(), cmp).get();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new MiException(e);
                } catch (ExecutionException e) {
                    throw Closables.exceptionAs(e.getCause(), MiException.class);
                }
            }
        };
    }
    
    public MappedSqlBuilder<Entity,?> getMappingBuilder() {
        return mapping;
    }
    
    public TemplateLayer<? super MappedStatement<Entity, ? extends SelectBuilder>> getSelectLayer() {
        TemplateLayer<Mapping<Entity>> mLayer = mapping.templates.getMappingBuilder().getMaterializationLayer();
        TemplateLayer<SelectBuilder> sLayer = mapping.templates.getSqlBuilder().getSelectLayer();
        return TemplateLayerStack.<MappedStatement<Entity, ? extends SelectBuilder>>join(
                MappedStatement.wrapped(mLayer),
                StatementHolder.wrapped(sLayer));
    }
    
    protected class MyMapping implements MappedSqlBuilderDelegator<Entity, MyMapping> {
        
        private final List<String> keys = new ArrayList<>();
        private final List<Function<Entity, ?>> keyGetters = new ArrayList<>();
        private final List<BiConsumer<Entity, Object>> keySetters = new ArrayList<>();
        private final MappedSqlTemplates<Entity> templates;

        public MyMapping() {
            templates = new MappedSqlTemplates<>();
        }

        public List<Function<Entity, ?>> getKeyGetters() {
            for (int i = keyGetters.size(); i < keys.size(); i++) {
                Function<Entity, ?> g = templates.getMappingBuilder().getGetters().get(keys.get(i));
                keyGetters.add(g);
            }
            return keyGetters;
        }

        public List<BiConsumer<Entity, Object>> getKeySetters() {
            for (int i = keySetters.size(); i < keys.size(); i++) {
                BiConsumer<Entity, Object> s = templates.getMappingBuilder().getSetters().get(keys.get(i));
                keySetters.add(s);
            }
            return keySetters;
        }

        @Override
        public MappedSqlBuilder<Entity,?> internalMappedSqlTemplatesBuilder() {
            return templates;
        }

        @Override
        public MyMapping attribute(ResultScope scope, boolean key, SqlAttribute attribute) {
            if (key) {
                keys.add(attribute.getKey());
            }
            return MappedSqlBuilderDelegator.super.attribute(scope, key, attribute);
        }
//
//        @Override
//        public <F> MyMapping attribute(ResultScope scope, boolean key, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
//            if (key) {
//                keys.add(id);
//                keyGetters.add(getter);
//                keySetters.add((BiConsumer<Entity, Object>) setter);
//            }
//            return MappedSqlBuilderDelegator.super.attribute(scope, key, id, attribute, getter, setter);
//        }

        public EntityConfiguration<Entity> attributeConfiguration(Iterable<String> attributes) {
            return templates.getMappingBuilder().attributeConfiguration(attributes);
        }
    }
}
