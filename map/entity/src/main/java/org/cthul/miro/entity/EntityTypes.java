package org.cthul.miro.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.CompletableBuilder;
import org.cthul.miro.util.CompleteAndClose;
import org.cthul.miro.util.XConsumer;
import org.cthul.miro.util.XSupplier;

/**
 * Contains utility methods related to entity types.
 */
public class EntityTypes {
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param configurations
     * @return combined configuration
     */
    @SafeVarargs
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity>... configurations) {
        return multiConfiguration(Arrays.asList(configurations));
    }
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param configurations
     * @return combined configuration
     */
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(Collection<EntityConfiguration<? super Entity>> configurations) {
        List<EntityConfiguration<? super Entity>> cfg = new ArrayList<>(configurations.size());
        collectConfigurations(configurations, cfg);
        if (cfg.isEmpty()) return noConfiguration();
        return new MultiConfiguration<>(cfg);
    }
    
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity> first, EntityConfiguration<? super Entity>[] more) {
        return multiConfiguration(first, Arrays.asList(more));
    }
    
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity> first, Collection<EntityConfiguration<? super Entity>> more) {
        List<EntityConfiguration<? super Entity>> cfg = new ArrayList<>(more.size());
        collectConfigurations(Arrays.asList(first), cfg);
        collectConfigurations(more, cfg);
        if (cfg.isEmpty()) return noConfiguration();
        return new MultiConfiguration<>(cfg);
    }
    
    private static <Entity> void collectConfigurations(Collection<EntityConfiguration<? super Entity>> configurations, List<EntityConfiguration<? super Entity>> target) {
        configurations.forEach(c -> {
            if (c instanceof MultiConfiguration) {
                target.addAll(((MultiConfiguration<Entity>) c).configurations);
            } else if (c != NO_CONFIGURATION) {
                target.add(c);
            }
        });
    }

    public static <Entity, T extends Throwable> EntityInitializer<Entity> buildInitializer(XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        InitBuilder<Entity> b = new InitBuilder<>();
        action.accept(b);
        return b.buildInitializer();
    }
    
    public static <Entity> EntityInitializer<Entity> buildInitializer(EntityConfiguration<Entity> cfg, MiResultSet rs) throws MiException {
        return buildInitializer(b -> cfg.newInitializer(rs, b));
    }
    
    public static <Entity, T extends Throwable> EntityInitializer<Entity> buildNestedInitializer(CompletableBuilder parent, XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        NestedInitBuilder<Entity> b = new NestedInitBuilder<>(parent);
        action.accept(b);
        return b.buildInitializer();
    }
    
    public static <Entity, T extends Throwable> EntityFactory<Entity> buildFactory(XConsumer<? super FactoryBuilder<Entity>, T> action) throws T {
        FacBuilder<Entity> b = new FacBuilder<>();
        action.accept(b);
        return b.buildFactory();
    }
    
    public static <Entity, T extends Throwable> EntityFactory<Entity> buildFactory(EntityType<Entity> type, MiResultSet rs) throws MiException {
        return buildFactory(b -> type.newFactory(rs, b));
    }
    
    public static <Entity, T extends Throwable> EntityFactory<Entity> buildNestedFactory(CompletableBuilder parent, XConsumer<? super FactoryBuilder<Entity>, T> action) throws T {
        NestedFacBuilder<Entity> b = new NestedFacBuilder<>(parent);
        action.accept(b);
        return b.buildFactory();
    }
    
    /**
     * Combines multiple initializers into one.
     * @param <Entity>
     * @param initializers
     * @return combined initializer
     */
    public static <Entity> EntityInitializer<Entity> multiInitializer(Collection<EntityInitializer<? super Entity>> initializers) {
       return buildInitializer(b -> initializers.forEach(i -> b.add(i)));
    }

    /**
     * Creates a configured type.
     * @param <Entity>
     * @param type
     * @param configurations
     * @return configured type
     */
    @SafeVarargs
    public static <Entity> EntityType<Entity> configuredType(EntityType<Entity> type, EntityConfiguration<? super Entity>... configurations) {
        return configuredType(type, Arrays.asList(configurations));
    }
    
    /**
     * Creates a configured type.
     * @param <Entity>
     * @param type
     * @param configurations
     * @return configured type
     */
    public static <Entity> EntityType<Entity> configuredType(EntityType<Entity> type, Collection<EntityConfiguration<? super Entity>> configurations) {
        List<EntityConfiguration<? super Entity>> cfg = new ArrayList<>();
        if (type instanceof ConfiguredType) {
            @SuppressWarnings("unchecked")
            ConfiguredType<Entity> ct = (ConfiguredType) type;
            cfg.addAll(ct.configurations);
            type = ct.type;
        }
        collectConfigurations(configurations, cfg);
        if (cfg.isEmpty()) return type;
        return new ConfiguredType<>(type, cfg);
    }
    
    /**
     * Creates an initializing factory.
     * @param <Entity>
     * @param factory
     * @param initializers
     * @return initializing factory
     */
    @SafeVarargs
    public static <Entity> EntityFactory<Entity> initializingFactory(EntityFactory<Entity> factory, EntityInitializer<? super Entity>... initializers) {
        return initializingFactory(factory, Arrays.asList(initializers));
    }
    
    public static <Entity> EntityFactory<Entity> initializingFactory(EntityFactory<Entity> factory, Collection<EntityInitializer<? super Entity>> initializers) {
        return buildFactory(b -> {
            b.set(factory);
            initializers.forEach(i -> b.add(i));
        });
    }
    
    public static <Entity> EntityFactory<Entity> batch(EntityFactory<Entity> factory) {
        return new BatchFactory<>(factory);
    }
    
    @SuppressWarnings("unchecked")
    public static <Entity> EntityConfiguration<Entity> noConfiguration() {
        return NO_CONFIGURATION;
    }
    
    @SuppressWarnings("unchecked")
    public static <Entity> EntityInitializer<Entity> noInitialization() {
        return NO_INITIALIZATION;
    }
    
    public static <Entity> EntityConfiguration<Entity> asConfiguration(EntityInitializer<Entity> init) {
        Objects.requireNonNull(init, "initializer");
        return new EntityConfiguration<Entity>() {
            @Override
            public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
                builder.add(init);
            }
            @Override
            public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
                return init;
            }
            @Override
            public String toString() {
                return String.valueOf(init);
            }
        };
    }
    
    public static <Entity> EntityConfiguration<Entity> subResultConfiguration(String prefix, EntityConfiguration<Entity> cfg) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(cfg, "configuration");
        return new EntityConfiguration<Entity>() {
            @Override
            public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
                MiResultSet rs = resultSet.subResult(prefix);
                builder.add(cfg, rs);
            }
            @Override
            public String toString() {
                return prefix + cfg;
            }
        };
    }
    
    public static <Entity> EntityType<Entity> asType(EntityFactory<Entity> factory) {
        Objects.requireNonNull(factory, "factory");
        return new EntityType<Entity>() {
            @Override
            public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
                builder.set(factory);
            }
            @Override
            public EntityFactory<Entity> newFactory(MiResultSet resultSet) throws MiException {
                return factory;
            }
            @Override
            public String toString() {
                return String.valueOf(factory);
            }
        };
    }
    
    @SuppressWarnings("rawtypes")
    private static final EntityInitializer NO_INITIALIZATION = new EntityInitializer<Object>() {
        @Override
        public void apply(Object entity) throws MiException { }
        @Override
        public void complete() throws MiException { }
        @Override
        public String toString() {
            return "no-op";
        }
    };

    @SuppressWarnings("rawtypes")
    private static final EntityConfiguration NO_CONFIGURATION = new EntityConfiguration<Object>() {
        @Override
        public EntityInitializer<Object> newInitializer(MiResultSet resultSet) throws MiException {
            return NO_INITIALIZATION;
        }
        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Object> builder) throws MiException {
        }
        @Override
        public String toString() {
            return "no-op";
        }
    };
    
    private static boolean blank(Collection<?> c) {
        return CompleteAndClose.blank(c);
    }
    
    protected static class MultiConfiguration<Entity> implements EntityConfiguration<Entity> {
        
        private final List<EntityConfiguration<? super Entity>> configurations;

        public MultiConfiguration(List<EntityConfiguration<? super Entity>> configurations) {
            this.configurations = configurations;
        }

        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
            for (EntityConfiguration<? super Entity> cfg: configurations) {
                builder.add(cfg, resultSet);
            }
        }

        @Override
        public String toString() {
            return configurations.stream().map(Object::toString)
                    .collect(Collectors.joining(", ", "(", ")"));
        }
    }
    
    protected static class ConfiguredType<Entity> implements EntityType<Entity> {

        private final EntityType<Entity> type;
        private final List<EntityConfiguration<? super Entity>> configurations;

        public ConfiguredType(EntityType<Entity> type, List<EntityConfiguration<? super Entity>> configurations) {
            this.type = type;
            this.configurations = configurations;
        }

        @Override
        public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
            FactoryBuilder<Entity> fb = builder.set(type, resultSet);
            for (EntityConfiguration<? super Entity> cfg: configurations) {
                fb.add(cfg, resultSet);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append(" with ");
            configurations.stream().forEach(c -> {
                sb.append(c).append(", ");
            });
            sb.setLength(sb.length()-2);
            return sb.toString();
        }
    }
    
    public static class NestedInitBuilder<Entity> 
                    extends CompleteAndClose.NestedCompletableBuilder<InitializationBuilder<Entity>> 
                    implements InitializationBuilder<Entity> {
        
        private List<XConsumer<? super Entity, ?>> consumers = null;

        public NestedInitBuilder(CompletableBuilder completableBuilder) {
            super(completableBuilder);
        }
        
        @Override
        public InitializationBuilder<Entity> add(EntityInitializer<? super Entity> initializer) {
            if (initializer == NO_INITIALIZATION) {
                return this;
            } else if (initializer instanceof CompositeInitializer) {
                CompositeInitializer<Entity> ci = (CompositeInitializer) initializer;
                if (consumers == null) consumers = new ArrayList<>();
                consumers.addAll(ci.consumers);
                addName(ci.completeAndClose);
                addCompleteAndClose(ci.completeAndClose);
                return this;
            } else {
                addInitializer(initializer::apply);
                addName(initializer);
                addCompleteAndClose(initializer);
            }
            return this;
        }

        @Override
        public InitializationBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer) {
            if (consumers == null) consumers = new ArrayList<>();
            consumers.add(initializer);
            return this;
        }

        @Override
        public InitializationBuilder<Entity> addCompletable(Completable completable) {
            if (completable instanceof CompositeInitializer) {
                return super.addCompletable(((CompositeInitializer) completable).completeAndClose);
            }
            return super.addCompletable(completable);
        }

        @Override
        public InitializationBuilder<Entity> addCloseable(AutoCloseable closeable) {
            if (closeable instanceof CompositeInitializer) {
                return super.addCloseable(((CompositeInitializer) closeable).completeAndClose);
            }
            return super.addCloseable(closeable);
        }

        @Override
        public InitializationBuilder<Entity> addName(Object name) {
            if (name instanceof CompositeInitializer) {
                return super.addName(((CompositeInitializer) name).completeAndClose);
            }
            return super.addName(name);
        }
        
        public EntityInitializer<Entity> buildInitializer() {
            CompleteAndClose cc = buildCompleteAndClose();
            if (cc == CompleteAndClose.NO_OP && blank(consumers)) {
                return NO_INITIALIZATION;
            }
            return new CompositeInitializer<>(consumers, cc);
        }
    }

    protected static class InitBuilder<Entity> extends NestedInitBuilder<Entity> {
        
        private final CompleteAndClose.Builder<?> ccBuilder;

        public InitBuilder() {
            this(new CompleteAndClose.Builder<>());
        }

        private InitBuilder(CompleteAndClose.Builder<?> ccBuilder) {
            super(ccBuilder);
            this.ccBuilder = ccBuilder;
        }

        @Override
        protected void addNestedName(Object name) {
            ccBuilder.addName(name);
        }

        @Override
        public CompleteAndClose buildCompleteAndClose() {
            return ccBuilder.buildCompleteAndClose();
        }
    }
    
    protected static class NestedFacBuilder<Entity> extends NestedInitBuilder<Entity> implements FactoryBuilder<Entity> {

        private XSupplier<? extends Entity, ?> factory = null;
        private Object factoryName = null;

        public NestedFacBuilder(CompletableBuilder completableBuilder) {
            super(completableBuilder);
        }

        @Override
        public <E extends Entity> FactoryBuilder<E> set(EntityFactory<E> factory) {
            if (factory instanceof CompositeFactory) {
                CompositeFactory<E> cf = (CompositeFactory<E>) factory;
                setFactory(cf.supplier).add(cf.setup);
            } else {
                setFactory(factory::newEntity);
                addCompletable(factory);
                addCloseable(factory);
            }
            return (FactoryBuilder) this;
        }

        @Override
        public <E extends Entity> FactoryBuilder<E> setFactory(XSupplier<E, ?> factory) {
            this.factory = factory;
            return (FactoryBuilder) this;
        }

        @Override
        public InitializationBuilder<Entity> addName(Object name) {
            if (factoryName == null) {
                factoryName = name;
                return this;
            } else {
                return super.addName(name);
            }
        }
        
        public EntityFactory<Entity> buildFactory() {
            if (factory == null) throw new NullPointerException("factory");
            return new CompositeFactory<>(factory, factoryName, buildInitializer());
        }

        @Override
        public String toString() {
            return (factoryName != null ? factoryName : "?") + 
                    " with " + super.toString();
        }
    }
    
    protected static class FacBuilder<Entity> extends NestedFacBuilder<Entity> {
        
        private final CompleteAndClose.Builder<?> ccBuilder;

        public FacBuilder() {
            this(new CompleteAndClose.Builder<>());
        }

        private FacBuilder(CompleteAndClose.Builder<?> ccBuilder) {
            super(ccBuilder);
            this.ccBuilder = ccBuilder;
        }

        @Override
        protected void addNestedName(Object name) {
            ccBuilder.addName(name);
        }

        @Override
        public CompleteAndClose buildCompleteAndClose() {
            return ccBuilder.buildCompleteAndClose();
        }
    }
    
    protected static class CompositeInitializer<Entity> implements EntityInitializer<Entity> {

        private final List<XConsumer<? super Entity, ?>> consumers;
        private final CompleteAndClose completeAndClose;

        public CompositeInitializer(List<XConsumer<? super Entity, ?>> consumers, CompleteAndClose completeAndClose) {
            this.consumers = blank(consumers) ? Collections.emptyList() : new ArrayList<>(consumers);
            this.completeAndClose = completeAndClose;
        }

        @Override
        public void apply(Entity entity) throws MiException {
            try {
                for (XConsumer<? super Entity, ?> c: consumers) {
                    c.accept(entity);
                }
            } catch (Throwable e) {
                throw Closeables.exceptionAs(e, MiException.class);
            }
        }

        @Override
        public void complete() throws MiException {
            try {
                completeAndClose.complete();
            } catch (Exception e) {
                throw Closeables.exceptionAs(e, MiException.class);
            }
        }

        @Override
        public void close() throws MiException {
            try {
                completeAndClose.close();
            } catch (Exception e) {
                throw Closeables.exceptionAs(e, MiException.class);
            }
        }

        @Override
        public String toString() {
            return completeAndClose.toString();
        }
    }
    
    protected static class CompositeFactory<Entity> implements EntityFactory<Entity> {

        private final XSupplier<? extends Entity, ?> supplier;
        private final Object factoryName;
        private final EntityInitializer<? super Entity> setup;

        public CompositeFactory(XSupplier<? extends Entity, ?> supplier, Object factoryName, EntityInitializer<? super Entity> setup) {
            this.supplier = supplier;
            this.factoryName = factoryName;
            this.setup = setup;
        }

        @Override
        public Entity newEntity() throws MiException {
            Entity e;
            try {
                e = supplier.get();
            } catch (Throwable t) {
                throw Closeables.exceptionAs(t, MiException.class);
            }
            setup.apply(e);
            return e;
        }

        @Override
        public void complete() throws MiException {
            setup.complete();
        }

        @Override
        public void close() throws MiException {
            setup.close();
        }

        @Override
        public String toString() {
            if (setup == NO_INITIALIZATION) {
                return String.valueOf(factoryName);
            }
            return String.valueOf(factoryName) + " with " + setup.toString();
        }
    }
    
    protected static class BatchFactory<Entity> implements EntityFactory<Entity> {
        
        private final EntityFactory<Entity> factory;

        public BatchFactory(EntityFactory<Entity> factory) {
            this.factory = factory;
        }
        
        @Override
        public Entity newEntity() throws MiException {
            return factory.newEntity();
        }

        @Override
        public void complete() throws MiException {
            factory.complete();
        }

        @Override
        public void close() throws MiException {
            complete();
        }

        @Override
        public EntityFactory<Entity> batch() {
            return this;
        }

        @Override
        public String toString() {
            return "batch " + factory;
        }
    }
}
