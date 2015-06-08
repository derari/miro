package org.cthul.miro.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.util.PrefixedResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Closables;

/**
 *
 */
public class EntityTypes {
    
    @SafeVarargs
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<Entity>... configurations) {
        return multiConfiguration(Arrays.asList(configurations));
    }
    
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(Collection<EntityConfiguration<Entity>> configurations) {
        List<EntityConfiguration<Entity>> cfg = new ArrayList<>();
        configurations.stream().forEach((c) -> {
            if (c instanceof MultiConfiguration) {
                cfg.addAll(((MultiConfiguration<Entity>) c).configurations);
            } else if (cfg != NO_CONFIGURATION) {
                cfg.add(c);
            }
        });
        if (cfg.isEmpty()) return noConfiguration();
        return new MultiConfiguration<>(cfg);
    }
    
    @SafeVarargs
    public static <Entity> EntityType<Entity> configuredType(EntityType<Entity> type, EntityConfiguration<? super Entity>... configurations) {
        return configuredType(type, Arrays.asList(configurations));
    }
    
    public static <Entity> EntityType<Entity> configuredType(EntityType<Entity> type, Collection<EntityConfiguration<? super Entity>> configurations) {
        List<EntityConfiguration<? super Entity>> cfg = new ArrayList<>();
        if (type instanceof ConfiguredType) {
            @SuppressWarnings("unchecked")
            ConfiguredType<Entity> ct = (ConfiguredType) type;
            cfg.addAll(ct.configurations);
            type = ct.type;
        }
        configurations.stream().forEach((c) -> {
            if (c instanceof MultiConfiguration) {
                cfg.addAll(((MultiConfiguration<? super Entity>) c).configurations);
            } else if (c != NO_CONFIGURATION) {
                cfg.add(c);
            }
        });
        if (cfg.isEmpty()) return type;
        return new ConfiguredType<>(type, cfg);
    }
    
    @SafeVarargs
    public static <Entity> EntityFactory<Entity> initializingFactory(EntityFactory<Entity> factory, EntityInitializer<? super Entity>... initializers) {
        return initializingFactory(factory, Arrays.asList(initializers));
    }
    
    public static <Entity> EntityFactory<Entity> initializingFactory(EntityFactory<Entity> factory, Collection<EntityInitializer<? super Entity>> initializers) {
        List<EntityInitializer<? super Entity>> cfg = new ArrayList<>();
        if (factory instanceof ConfiguredFactory) {
            @SuppressWarnings("unchecked")
            ConfiguredFactory<Entity> ct = (ConfiguredFactory) factory;
            cfg.addAll(ct.initializers);
            factory = ct.factory;
        }
        initializers.stream().forEach((c) -> {
            if (c instanceof MultiInitializer) {
                cfg.addAll(((MultiInitializer<? super Entity>) c).initializers);
            } else if (c != NO_INITIALIZATION) {
                cfg.add(c);
            }
        });
        if (cfg.isEmpty()) return factory;
        return new ConfiguredFactory<>(factory, cfg);
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
    
    public static <Entity> EntityConfiguration<Entity> subResultConfiguration(String prefix, EntityConfiguration<Entity> cfg) {
        return rs -> {
            rs = new PrefixedResultSet(prefix, rs);
            return cfg.newInitializer(rs);
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
        @SuppressWarnings("unchecked")
        public EntityInitializer<Object> newInitializer(MiResultSet rs) throws MiException {
            return NO_INITIALIZATION;
        }
        @Override
        public String toString() {
            return "no-op";
        }
    };
    
    protected static class MultiConfiguration<Entity> implements EntityConfiguration<Entity> {
        
        private final List<EntityConfiguration<Entity>> configurations;

        public MultiConfiguration(List<EntityConfiguration<Entity>> configurations) {
            this.configurations = configurations;
        }

        @Override
        public EntityInitializer<Entity> newInitializer(MiResultSet rs) throws MiException {
            List<EntityInitializer<Entity>> initializers = new ArrayList<>(configurations.size());
            try {
                for (EntityConfiguration<Entity> cfg: configurations) {
                    initializers.add(cfg.newInitializer(rs));
                }
            } catch (MiException e) {
                throw Closables.closeAll(e, initializers);
            }
            return new MultiInitializer<>(initializers);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            configurations.stream().forEach(c -> {
                sb.append(c).append(", ");
            });
            sb.setLength(sb.length()-2);
            return sb.append(')').toString();
        }
    }
    
    protected static class MultiInitializer<Entity> implements EntityInitializer<Entity> {

        private final List<EntityInitializer<Entity>> initializers;

        public MultiInitializer(List<EntityInitializer<Entity>> initializers) {
            this.initializers = initializers;
        }
        
        @Override
        public void apply(Entity entity) throws MiException {
            for (EntityInitializer<Entity> i: initializers) {
                i.apply(entity);
            }
        }

        @Override
        public void complete() throws MiException {
            for (EntityInitializer<Entity> i: initializers) {
                i.complete();
            }
        }

        @Override
        public void close() throws MiException {
            complete();
            Closables.closeAll(MiException.class, initializers);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            initializers.stream().forEach(i -> {
                sb.append(i).append(", ");
            });
            sb.setLength(sb.length()-2);
            return sb.append(')').toString();
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
        public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
            List<EntityInitializer<? super Entity>> initializers = new ArrayList<>(configurations.size());
            EntityFactory<Entity> factory;
            try {
                for (EntityConfiguration<? super Entity> cfg: configurations) {
                    initializers.add(cfg.newInitializer(rs));
                }
                factory = type.newFactory(rs);
            } catch (MiException e) {
                throw Closables.closeAll(e, initializers);
            }
            return new ConfiguredFactory<>(factory, initializers);
        }

        @Override
        public Entity[] newArray(int length) {
            return type.newArray(length);
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
    
    protected static class ConfiguredFactory<Entity> implements EntityFactory<Entity> {

        private final EntityFactory<Entity> factory;
        private final List<EntityInitializer<? super Entity>> initializers;

        public ConfiguredFactory(EntityFactory<Entity> factory, List<EntityInitializer<? super Entity>> initializers) {
            this.factory = factory;
            this.initializers = initializers;
        }
        
        @Override
        public Entity newEntity() throws MiException {
            Entity e = factory.newEntity();
            for (EntityInitializer<? super Entity> i: initializers) {
                i.apply(e);
            }
            return e;
        }

        @Override
        public void complete() throws MiException {
            factory.complete();
            Closables.completeAll(MiException.class, initializers);
        }

        @Override
        public void close() throws MiException {
            try {
                factory.close();
            } catch (MiException e) {
                throw Closables.closeAll(e, initializers);
            }
            Closables.closeAll(MiException.class, initializers);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(factory).append(" with ");
            initializers.stream().forEach(i -> {
                sb.append(i).append(", ");
            });
            sb.setLength(sb.length()-2);
            return sb.toString();
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
