package org.cthul.miro.map.sql;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.sql.SqlQueryKey;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.entity.AttributeConfiguration;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.impl.AbstractEntityNodeType;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.impl.MappedQueryComposer;
import org.cthul.miro.util.Closables;
import org.cthul.miro.view.impl.CRUDTemplatesStack;

/**
 *
 * @param <Entity>
 */
public class MappedEntityType<Entity> extends AbstractEntityNodeType<Entity> implements AttributeConfiguration<Entity> {
    
//    private final GraphSchema schema;
    private final Class<Entity> clazz;
    private boolean initialized = false;
    private final Mapping mapping = new Mapping();
    private Constructor<Entity> constructor = null;

    public MappedEntityType(GraphSchema schema, Class<Entity> clazz) {
        super(clazz.getSimpleName());
//        this.schema = schema;
        this.clazz = clazz;
    }
    
    public void initialize() {
        initialized = true;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw Closables.unchecked(e);
        }
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
            e = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw Closables.unchecked(ex);
        }
        if (key == null) return e;
        if (key.length != mapping.keySetters.size()) {
            throw new IllegalArgumentException(
                    "Expected " + mapping.keySetters.size() + " key values, "
                            + "got " + key.length);
        }
        for (int i = 0; i < key.length; i++) {
            mapping.keySetters.get(i).accept(e, key[i]);
        }
        return e;
    }

    @Override
    protected Object[] getKey(Entity e, Object[] array) {
        requireInitialized();
        if (array == null) array = new Object[mapping.keys.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = mapping.keyGetters.get(i).apply(e);
        }
        return array;
    }

    @Override
    protected KeyReader newKeyReader(MiResultSet resultSet) throws MiException {
        requireInitialized();
        return newKeyReader(resultSet, mapping.keys);
    }

    @Override
    protected BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
        return new AbstractBatchLoader() {
            @Override
            protected void fillAttributes(List<Object[]> keys) throws MiException {
                MappedQueryComposer<Entity, SelectQuery> cmp = 
                        new MappedQueryComposer<>(getType(), SqlDQML.DQML.SELECT,
                        mapping.asTemplates().selectTemplate());
                cmp.requireAll(graph, attributes, ComposerKey.FETCH_KEYS);
                cmp.node(SqlQueryKey.FIND_BY_KEYS).addAll(keys);
                cmp.execute().noResult();
            }
        };
    }

    @Override
    public EntityConfiguration<Entity> forAttributes(List<?> attributes) {
        return mapping.attributeConfiguration(flattenStr(attributes));
    }

    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs, List<?> attributes) throws MiException {
        return forAttributes(attributes).newInitializer(rs);
    }


//    @Override
//    protected EntityConfiguration<Entity> newAttributeSetter(GraphApi graph, List<?> attributes) throws MiException {
//        requireInitialized();
//        return mapping.templates.getMappingBuilder().attributeConfiguration(flattenStr(attributes));
//    }
    
    public MappedSqlTemplatesBuilder<Entity> getMappingBuilder() {
        return mapping;
    }
    
    protected class Mapping extends MappedSqlTemplatesDelegator<Entity> {
        
        private final List<String> keys = new ArrayList<>();
        private final List<Function<Entity, ?>> keyGetters = new ArrayList<>();
        private final List<BiConsumer<Entity, Object>> keySetters = new ArrayList<>();
        private final MappedSqlTemplates<Entity> templates;
        private CRUDTemplatesStack theTemplateStack;

        public Mapping() {
            super(new MappedSqlTemplates<>());
            templates = (MappedSqlTemplates<Entity>) getDelegatee();
        }

        @Override
        public <F> MappedSqlTemplatesBuilder<Entity> attribute(ResultScope scope, boolean key, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
            if (key) {
                keys.add(id);
                keyGetters.add(getter);
                keySetters.add((BiConsumer<Entity, Object>) setter);
            }
            return super.attribute(scope, key, id, attribute, getter, setter);
        }

        public EntityConfiguration<Entity> attributeConfiguration(Iterable<String> attributes) {
            return templates.getMappingBuilder().attributeConfiguration(attributes);
        }
        
        public CRUDTemplatesStack<?, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, ?, ?> getTemplateStack() {
            if (theTemplateStack == null) {
                return asTemplates();
            }
            return theTemplateStack;
        }

        @Override
        public CRUDTemplatesStack<MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>> asTemplates() {
            return theTemplateStack = super.asTemplates();
        }
    }
}
