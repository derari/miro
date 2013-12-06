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
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.result.*;

public class AbstractMappedQuery<Entity> extends AbstractQuery {
    
    private final MappedTemplateProvider<Entity> mtp;
    private final Mapping<Entity> mapping;
    private final List<Entity> entities = new ArrayList<>();
//    private Graph graph = null;

    public AbstractMappedQuery(QueryType<?> queryType, Mapping<Entity> mapping) {
        super(queryType);
        this.mtp = null;
        this.mapping = mapping;
    }

    public AbstractMappedQuery(QueryType<?> queryType, QueryTemplate template) {
        super(queryType, template);
        this.mtp = null;
        this.mapping = null;
    }

    public AbstractMappedQuery(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider) {
        super(type, templateProvider);
        this.mtp = templateProvider;
        this.mapping = templateProvider.getMapping();
    }
    
//    protected void setGraph(Graph graph) {
//        this.graph = graph;
//    }
//    
//    protected synchronized Graph graph() {
//        if (graph == null) graph = new Graph();
//        return graph;
//    }
    
    protected void addToGraph(List<Entity> entities) {
        this.entities.addAll(entities);
    }
    
    protected void addToGraph(Entity... entities) {
        this.entities.addAll(Arrays.asList(entities));
//        Graph.EntitySet<Entity> es = graph().entityType(mtp);
//        for (Entity e: entities) {
//            es.put(e);
//        }
    }
    
    protected EntityType<Entity> getEntityType() {
//        if (graph != null) {
//            if (mtp == null) {
//                throw new IllegalStateException(
//                        "Has graph but no graph adapter");
//            }
//            return graph.entityType(mtp);
//        }
        if (entities.isEmpty()) {
            return mapping;
        } else {
            return typeForEntities(entities);
        }
    }
    
    protected EntityType<Entity> typeForEntities(List<Entity> entities) {
        return graphTypeForEntities(entities);
    }
    
    protected EntityType<Entity> graphTypeForEntities(List<Entity> entities) {
        Graph graph = new Graph();
        Graph.EntitySet<Entity> es = graph.entityType(mtp);
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
        List<EntityConfiguration<? super Entity>> configs = new ArrayList<>();
        configs.add(getFieldsConfiguration(cnn));
        for (Object o: getParts()) {
            if (o instanceof ConfigurationProvider) {
                ConfigurationProvider<Entity> cp = (ConfigurationProvider<Entity>) o;
                configs.add(cp.getConfiguration(cnn, mapping));
            }
        }
        return CombinedEntityConfig.combine(configs);
    }
    
    protected ResultSet executeJdbc(MiConnection cnn) throws SQLException {
        JdbcQuery<?> query = getAdapter(cnn.getJdbcAdapter());
        System.out.println(query);
        return cnn.execute(query);
    }
    
    protected MiFuture<ResultSet> submitJdbc(final MiConnection cnn) {
        MiFutureAction<AbstractMappedQuery<?>, ResultSet> exec = new MiFutureAction<AbstractMappedQuery<?>, ResultSet>() {
            @Override
            public ResultSet call(AbstractMappedQuery<?> arg) throws Exception {
                return arg.executeJdbc(cnn);
            }
        };
        return cnn.submit(exec, this);
    }
}
