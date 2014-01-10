package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphConfigurationProvider;
import org.cthul.miro.query.*;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.parts.*;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.UniqueKey;
import org.cthul.miro.result.*;

public class AbstractMappedQuery<Entity> extends AbstractQuery {
    
    protected final MappedTemplateProvider<Entity> mappedProvider;
    private final Mapping<Entity> mapping;
    private List<Entity> entities = null;
    private List<ConfigurationQueryPart> configurations = null;
    private Graph graph = null;

    public AbstractMappedQuery(QueryType<?> queryType, Mapping<Entity> mapping) {
        super(queryType);
        this.mappedProvider = null;
        this.mapping = mapping;
    }

    public AbstractMappedQuery(QueryType<?> queryType, QueryTemplate template) {
        super(queryType, template);
        this.mappedProvider = null;
        this.mapping = null;
    }

    public AbstractMappedQuery(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider) {
        super(type, templateProvider);
        this.mappedProvider = templateProvider;
        this.mapping = templateProvider.getMapping();
    }
    
    protected void setGraph(Graph graph) {
        if (this.graph != null) {
            throw new IllegalStateException("Graph was already set");
        }
        this.graph = graph;
    }
    
    protected void configure(Object config) {
        configure(new UniqueKey("configure"), config);
    }

    protected void configure(Object key, Object config) {
        EntityConfigurationPart<?> part = new EntityConfigurationPart<>(config, key);
        addPart(OtherQueryPart.VIRTUAL, part);
    }
    
    @Override
    protected synchronized void addPart(QueryPartType partType, QueryPart part) {
        if (part instanceof ConfigurationQueryPart) {
            ConfigurationQueryPart cp = (ConfigurationQueryPart) part;
            if (configurations == null) configurations = new ArrayList<>();
            configurations.add(cp);
            if (cp.getConfiguration() instanceof GraphConfigurationProvider) {
                if (graph == null) graph = new Graph();
            }
        }
        super.addPart(partType, part);
    }
    
    protected Graph graph() {
        if (graph == null) graph = new Graph();
        return graph;
    }
    
    protected void addToGraph(List<Entity> entities) {
        if (this.entities == null) this.entities = new ArrayList<>();
        this.entities.addAll(entities);
    }
    
    protected void addToGraph(Entity... entities) {
        addToGraph(Arrays.asList(entities));
//        Graph.EntitySet<Entity> es = graph().entityType(mappedProvider);
//        for (Entity e: entities) {
//            es.put(e);
//        }
    }
    
    protected EntityType<Entity> getEntityType() {
//        if (graph != null) {
//            if (mappedProvider == null) {
//                throw new IllegalStateException(
//                        "Has graph but no graph adapter");
//            }
//            return graph.entityType(mappedProvider);
//        }
        if (graph == null && (entities == null || entities.isEmpty())) {
            return mapping;
        } else {
            return typeForEntities(entities);
        }
    }
    
    protected EntityType<Entity> typeForEntities(List<Entity> entities) {
        return graphTypeForEntities(entities);
    }
    
    protected EntityType<Entity> graphTypeForEntities(List<Entity> entities) {
        Graph.EntitySet<Entity> es = graph().entityType(mappedProvider);
        for (Entity e: entities) {
            es.put(e);
        }
        return es;
    }
    
    protected EntityType<Entity> entitiesInOrder(final List<Entity> entities) {
        return new EntityType<Entity>() {
            @Override
            public EntityFactory<Entity> newFactory(ResultSet rs) throws SQLException {
                final Iterator<Entity> it = entities.iterator();
                return new EntityFactory<Entity>() {
                    @Override
                    public Entity newEntity() throws SQLException {
                        return it.next();
                    }
                    @Override
                    public Entity newCursorValue(ResultCursor<? super Entity> rc) throws SQLException {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public Entity copy(Entity e) throws SQLException {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void close() throws SQLException { }
                };
            }
            @Override
            public Entity[] newArray(int length) {
                return mapping.newArray(length);
            }
        };
    }
    
    protected EntityConfiguration<? super Entity> getFieldsConfiguration(MiConnection cnn) {
        return mapping.newFieldConfiguration(getResultAttributes());
    }
    
    protected EntityConfiguration<? super Entity> getConfiguration(MiConnection cnn) {
        if (configurations == null || configurations.isEmpty()) {
            return getFieldsConfiguration(cnn);
        }
        List<EntityConfiguration<? super Entity>> configs = new ArrayList<>();
        configs.add(getFieldsConfiguration(cnn));
        for (ConfigurationQueryPart c: configurations) {
            Object o = c.getConfiguration();
            Object[] args = c.getArguments();
            final EntityConfiguration<? super Entity> ec;
            if (o instanceof GraphConfigurationProvider) {
                GraphConfigurationProvider<Entity> cp = (GraphConfigurationProvider) o;
                ec = cp.getConfiguration(cnn, mapping, graph(), args);
            } else {
                ec = ConfigurationInstance.asConfiguration(o, cnn, mapping, args);
            }
            configs.add(ec);
        }
        return CombinedEntityConfig.combine(configs);
    }
    
    protected ResultSet executeJdbc(MiConnection cnn) throws SQLException {
        JdbcQuery<?> query = getAdapter(cnn.getJdbcAdapter());
        return cnn.execute(query);
    }
    
    protected MiFuture<ResultSet> submitJdbc(final MiConnection cnn) {
        MiFutureAction<AbstractMappedQuery<?>, ResultSet> exec = new MiFutureAction<AbstractMappedQuery<?>, ResultSet>() {
            @Override
            public ResultSet call(AbstractMappedQuery<?> arg) throws Exception {
                return arg.executeJdbc(cnn);
            }
        };
        return cnn.submit(this, exec);
    }

    @Override
    protected Internal newInternal() {
        return new Internal();
    }
    
    protected class Internal extends AbstractQuery.Internal implements MappedInternalQueryBuilder {

        @Override
        public void configure(Object config) {
            AbstractMappedQuery.this.configure(config);
        }

        @Override
        public void configure(Object key, Object config) {
            AbstractMappedQuery.this.configure(key, config);
        }
    }
    
    protected static class EntityConfigurationPart<Entity> extends AbstractQueryPart implements ConfigurationQueryPart {
        
        private final Object cfg;
        private Object[] args = null;

        public EntityConfigurationPart(Object cfg, Object key) {
            super(key);
            this.cfg = cfg;
        }

        @Override
        public void put(Object key, Object... args) {
            if (key == null || "".equals(key)) {
                this.args = args;
            } else {
                super.put(key, args);
            }
        }

        @Override
        public Object getConfiguration() {
            return cfg;
        }

        @Override
        public Object[] getArguments() {
            return args;
        }
    }
}
