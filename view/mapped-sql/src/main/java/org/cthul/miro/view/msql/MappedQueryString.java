package org.cthul.miro.view.msql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.impl.MiQueryQlBuilder;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.stmt.MiStatement;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.entity.EntityAttributes;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.result.Results;
import org.cthul.miro.util.Closables;
import org.cthul.miro.view.ViewR;

/**
 *
 * @param <Entity>
 */
public class MappedQueryString<Entity> implements ViewR<MappedQueryString.Query<Entity>> {

    public static interface Query<Entity> extends MiStatement<Results<Entity>> {
        
        default Results.Action<Entity> with(Object... args) {
            return Query.this.with(Arrays.asList(args));
        }
        
        Results.Action<Entity> with(Iterable<?> args);

        @Override
        Results.Action<Entity> asAction();
    }
    
    public static <Entity> Builder<Entity> forType(EntityType<Entity> entityType) {
        return new Builder<Entity>().forType(entityType);
    }

    public static <Entity> Builder<Entity> forType(Class<?> clazz) {
        return new Builder<Entity>().forType(clazz);
    }

    public static <Entity> Builder<Entity> forType(Graph graph, Class<Entity> clazz) {
        return new Builder<Entity>().forType(graph, clazz);
    }

    public static <Entity> Builder<Entity> forType(GraphSchema schema, Class<Entity> clazz) {
        return new Builder<Entity>().forType(schema, clazz);
    }

    public static <Entity> Builder<Entity> forType(EntityType<Entity> defaultType, Class<?> clazz) {
        return new Builder<Entity>().forType(defaultType, clazz);
    }
    
    private final QlCode queryString;
    private final MiFunction<MiConnection, EntityType<Entity>> typeProvider;

    public MappedQueryString(QlCode queryString, MiFunction<MiConnection, EntityType<Entity>> typeProvider) {
        this.queryString = queryString;
        this.typeProvider = typeProvider;
    }

    @Override
    public Query<Entity> select(List<?> attributes) {
        MiConnection cnn = null;
        for (Object o: attributes) {
            if (o instanceof MiConnection) {
                cnn = (MiConnection) o;
            } else if (o != null) {
                throw new IllegalArgumentException(String.valueOf(o));
            }
        }
        return select(cnn);
    }
    
    public Query<Entity> select(MiConnection cnn) {
        if (cnn == null) throw new NullPointerException("connection");
        return new QueryImpl(cnn);
    }
    
    protected class QueryImpl implements Query<Entity> {
        
        final MiConnection cnn;
        final List<Object> args = new ArrayList<>();

        public QueryImpl(MiConnection cnn) {
            this.cnn = cnn;
        }
        
        @Override
        public Results.Action<Entity> with(Iterable<?> args) {
            args.forEach(this.args::add);
            return asAction();
        }

        protected MiQueryQlBuilder buildStatement() {
            MiQueryQlBuilder builder = MiQueryQlBuilder.create(cnn);
            builder.append(queryString);
            builder.pushArguments(args);
            return builder;
        }

        @Override
        public Results<Entity> execute() throws MiException {
            MiResultSet rs = buildStatement().execute();
            try {
                EntityType<Entity> type = typeProvider.apply(cnn);
                return new Results<>(rs, type);
            } catch (Throwable t) {
                throw Closables.exceptionAs(t, MiException.class);
            }
        }

        @Override
        public Results.Action<Entity> asAction() {
            return buildStatement().asAction()
                    .andThen(Results.build(() -> typeProvider.apply(cnn)));
        }
    }
    
    private static final List<?> ALL = Arrays.asList("*");
    private static final List<?> TRY_ALL = Arrays.asList("*");
    
    public static class Builder<Entity> {
        
        private Graph defGraph = null;
        private EntityType<Entity> defType = null;
        private EntityAttributes<Entity> attributeCfg = null;
        private EntityConfiguration<Entity> configuration = EntityTypes.noConfiguration();
        private Object typeKey = null;
        private List<?> attributes = TRY_ALL;
        private QlCode query = null;

        public Builder() {
        }

        public Builder<Entity> forType(EntityType<Entity> defType) {
            this.defType = defType;
            return this;
        }
        
        public Builder<Entity> forType(Class<?> clazz) {
            this.typeKey = clazz;
            return this;
        }
        
        public Builder<Entity> forType(Graph graph, Class<Entity> clazz) {
            this.defGraph = graph;
            this.typeKey = clazz;
            return this;
        }
        
        public Builder<Entity> forType(GraphSchema schema, Class<Entity> clazz) {
            return forType(schema.newFakeGraph(null), clazz);
        }
        
        public Builder<Entity> forType(EntityType<Entity> defType, Class<?> clazz) {
            this.defType = defType;
            this.typeKey = clazz;
            return this;
        }
        
        public Builder<Entity> with(EntityConfiguration<Entity> cfg) {
            this.configuration = this.configuration.and(cfg);
            return this;
        }
        
        public Builder<Entity> allAttributes(EntityAttributes<Entity> cfg) {
            this.attributeCfg = cfg;
            return allAttributes();
        }
        
        public Builder<Entity> allAttributes() {
            attributes = ALL;
            return this;
        }
        
        public Builder<Entity> attributes(EntityAttributes<Entity> cfg, Object... attributes) {
            this.attributeCfg = cfg;
            return attributes(attributes);
        }
        
        public Builder<Entity> attributes(Object... attributes) {
            this.attributes = Arrays.asList(attributes);
            return this;
        }
        
        public Builder<Entity> query(String sql) {
            query = MiSqlParser.parseExpression(sql);
            return this;
        }

        protected EntityConfiguration<Entity> getConfiguration() {
            return configuration;
        }
        
        protected void prepareArguments() {
            try {
                if (defType == null && typeKey != null && defGraph != null) {
                    defType = defGraph.entityType(typeKey);
                }
                if (attributes != null) {
                    if (attributeCfg == null && (defType instanceof EntityAttributes)) {
                        attributeCfg = (EntityAttributes<Entity>) defType;
                    }
                    if (attributeCfg != null) {
                        if (attributes.equals(ALL)) {
                            with(attributeCfg.star());
                        } else {
                            with(attributeCfg.newConfiguration(attributes));
                        }
                        attributeCfg = null;
                        attributes = null;
                    }
                }
            } catch (MiException e) {
                throw Closables.unchecked(e);
            }
        }
        
        public MappedQueryString<Entity> build() {
            prepareArguments();
            MiFunction<MiConnection, EntityType<Entity>> typeProvider;
            if (typeKey == null) {
                if (defGraph != null) {
                    throw new IllegalStateException(
                            "Graph requires class identifier");
                }
                if (defType == null) {
                    throw new IllegalStateException(
                            "Entity type required");
                }
                if (attributes != null) {
                    throw new IllegalStateException(
                            "Attribute configuration required");
                }
                final EntityType<Entity> theType = defType.with(configuration);
                typeProvider = cnn -> theType;
            } else {
                typeProvider = new TypeProvider<>(defGraph, defType, typeKey, attributes, configuration);
            }
            return new MappedQueryString<>(query, typeProvider);
        }
    }

    private static class TypeProvider<Entity> implements MiFunction<MiConnection, EntityType<Entity>> {

        private final Graph defGraph;
        private final EntityType<Entity> defType;
        private final Object typeKey;
        private final List<?> attributes;
        private final EntityConfiguration<Entity> configuration;

        public TypeProvider(Graph defGraph, EntityType<Entity> defType, Object typeKey, List<?> attributes, EntityConfiguration<Entity> configuration) {
            this.defGraph = defGraph;
            this.defType = defType;
            this.typeKey = typeKey;
            this.attributes = attributes;
            this.configuration = configuration;
        }
        
        @Override
        public EntityType<Entity> call(MiConnection cnn) throws Throwable {
            Graph g = (cnn instanceof Graph) ? (Graph) cnn : defGraph;
            if (g == null && defType == null) {
                throw new IllegalArgumentException("Graph required");
            }
            EntityType<Entity> type = (g != null) ? g.entityType(typeKey) : defType;
            EntityConfiguration<Entity> cfg = configuration;
            if (attributes != null) {
                EntityAttributes<Entity> aCfg = null;
                if (type instanceof EntityAttributes) {
                    aCfg = (EntityAttributes<Entity>) type;
                }
                if (aCfg == null && attributes != TRY_ALL) {
                    throw new IllegalArgumentException(
                            "Attribute configuration required");
                }
                if (aCfg != null) {
                    if (attributes.equals(ALL)) {
                        cfg = cfg.and(aCfg.star());
                    } else {
                        cfg = cfg.and(aCfg.newConfiguration(attributes));
                    }
                }
            }
            type = type.with(cfg);
            return type;
        }
    }
    
    static class AllAttributesConfiguration<Entity> implements EntityConfiguration<Entity> {
        final EntityAttributes<Entity> aCfg;

        public AllAttributesConfiguration(EntityAttributes<Entity> aCfg) {
            this.aCfg = aCfg;
        }

        @Override
        public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
            return aCfg.newInitializer(resultSet, resultSet.listColumns());
        }
    }
}
