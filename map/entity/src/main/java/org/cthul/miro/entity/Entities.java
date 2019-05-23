package org.cthul.miro.entity;

import org.cthul.miro.entity.builder.BasicInitializationBuilder;
import org.cthul.miro.entity.builder.NestedFactoryBuilder;
import org.cthul.miro.entity.builder.BasicFactoryBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.builder.*;
import org.cthul.miro.util.CompletableBuilder;
import org.cthul.miro.util.XConsumer;

/**
 * Contains utility methods related to entity templates and configurations.
 */
public class Entities {
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param configurations
     * @return combined configuration
     */
    @SafeVarargs
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity>... configurations) {
        return Entities.<Entity>multiConfiguration(Arrays.asList(configurations));
    }
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param configurations
     * @return combined configuration
     */
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(Collection<EntityConfiguration<? super Entity>> configurations) {
        List<EntityConfiguration<? super Entity>> cfgList = new ArrayList<>(configurations.size());
        collectConfigurations(configurations, cfgList);
        if (cfgList.isEmpty()) return noConfiguration();
        if (cfgList.size() == 1) return (EntityConfiguration) cfgList.get(0);
        return new MultiConfiguration<>(cfgList);
    }
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param first
     * @param more
     * @return combined configuration
     */
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity> first, EntityConfiguration<? super Entity>[] more) {
        return multiConfiguration(first, Arrays.asList(more));
    }
    
    /**
     * Combines multiple configurations into one.
     * @param <Entity>
     * @param first
     * @param more
     * @return combined configuration
     */
    public static <Entity> EntityConfiguration<Entity> multiConfiguration(EntityConfiguration<? super Entity> first, Collection<EntityConfiguration<? super Entity>> more) {
        List<EntityConfiguration<? super Entity>> cfgList = new ArrayList<>(more.size());
        collectConfigurations(first, cfgList);
        collectConfigurations(more, cfgList);
        if (cfgList.isEmpty()) return noConfiguration();
        if (cfgList.size() == 1) return (EntityConfiguration) cfgList.get(0);
        return new MultiConfiguration<>(cfgList);
    }
    
    private static <Entity> void collectConfigurations(Collection<EntityConfiguration<? super Entity>> configurations, List<EntityConfiguration<? super Entity>> target) {
        configurations.forEach(c -> collectConfigurations(c, target));
    }

    private static <Entity> void collectConfigurations(EntityConfiguration<? super Entity> configuration, List<EntityConfiguration<? super Entity>> target) {
        if (configuration instanceof MultiConfiguration) {
            target.addAll(((MultiConfiguration<Entity>) configuration).configurations);
        } else if (configuration != NO_CONFIGURATION) {
            target.add(configuration);
        }
    }

    /**
     * Passes an {@link InitializationBuilder} to the given {@code action}
     * and returns the initializer.
     * @param <Entity>
     * @param <T>
     * @param action
     * @return entity initializer
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntityInitializer<Entity> buildInitializer(XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        BasicInitializationBuilder<Entity> b = new BasicInitializationBuilder<>();
        action.accept(b);
        return b.buildInitializer();
    }
    
    /**
     * Passes an {@link InitializationBuilder} to the given {@code action}
     * and returns the initializer.
     * Completing or closing the parent object will also complete or close
     * the nested initializer.
     * @param <Entity>
     * @param <T>
     * @param parent
     * @param action
     * @return nested entity initializer
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntityInitializer<Entity> buildNestedInitializer(CompletableBuilder parent, XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        NestedInitializationtBuilder<Entity> b = new NestedInitializationtBuilder<>(parent);
        action.accept(b);
        return b.buildInitializer();
    }
    
    /**
     * Passes an {@link FactoryBuilder} to the given {@code action}
     * and returns the factory.
     * @param <Entity>
     * @param <T>
     * @param action
     * @return entity factory
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntityFactory<Entity> buildFactory(XConsumer<? super FactoryBuilder<Entity>, T> action) throws T {
        BasicFactoryBuilder<Entity> b = new BasicFactoryBuilder<>();
        action.accept(b);
        return b.buildFactory();
    }
    
    /**
     * Passes an {@link FactoryBuilder} to the given {@code action}
     * and returns the factory.
     * Completing or closing the parent object will also complete or close
     * the nested factory.
     * @param <Entity>
     * @param <T>
     * @param parent
     * @param action
     * @return nested enttiy factory
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntityFactory<Entity> buildNestedFactory(CompletableBuilder parent, XConsumer<? super FactoryBuilder<Entity>, T> action) throws T {
        NestedFactoryBuilder<Entity> b = new NestedFactoryBuilder<>(parent);
        action.accept(b);
        return b.buildFactory();
    }
    
    /**
     * Passes an {@link SelectorBuilder} to the given {@code action}
     * and returns the selector.
     * @param <Entity>
     * @param <T>
     * @param action
     * @return entity selector
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntitySelector<Entity> buildSelector(XConsumer<? super SelectorBuilder<Entity>, T> action) throws T {
        BasicSelectorBuilder<Entity> b = new BasicSelectorBuilder<>();
        action.accept(b);
        return b.buildSelector();
    }
    
    /**
     * Passes an {@link SelectorBuilder} to the given {@code action}
     * and returns the selector.
     * Completing or closing the parent object will also complete or close
     * the nested selector.
     * @param <Entity>
     * @param <T>
     * @param parent
     * @param action
     * @return nested enttiy selector
     * @throws T 
     */
    public static <Entity, T extends Throwable> EntitySelector<Entity> buildNestedSelector(CompletableBuilder parent, XConsumer<? super SelectorBuilder<Entity>, T> action) throws T {
        NestedSelectorBuilder<Entity> b = new NestedSelectorBuilder<>(parent);
        action.accept(b);
        return b.buildSelector();
    }
    
//    /**
//     * Combines multiple initializers into one.
//     * @param <Entity>
//     * @param initializers
//     * @return combined initializer
//     */
//    public static <Entity> EntityInitializer<Entity> multiInitializer(Collection<EntityInitializer<? super Entity>> initializers) {
//       return buildInitializer(b -> initializers.forEach(i -> b.add(i)));
//    }

    /**
     * Creates a configured template.
     * @param <Entity>
     * @param template
     * @param configurations
     * @return configured template
     */
    @SafeVarargs
    public static <Entity> EntityTemplate<Entity> configuredTemplate(EntityTemplate<Entity> template, EntityConfiguration<? super Entity>... configurations) {
        return configuredTemplate(template, Arrays.asList(configurations));
    }
    
    /**
     * Creates a configured template.
     * @param <Entity>
     * @param template
     * @param configurations
     * @return configured template
     */
    public static <Entity> EntityTemplate<Entity> configuredTemplate(EntityTemplate<Entity> template, Collection<EntityConfiguration<? super Entity>> configurations) {
        List<EntityConfiguration<? super Entity>> cfg = new ArrayList<>();
        if (template instanceof ConfiguredTemplate) {
            @SuppressWarnings("unchecked")
            ConfiguredTemplate<Entity> ct = (ConfiguredTemplate) template;
            cfg.addAll(ct.configurations);
            template = ct.template;
        }
        collectConfigurations(configurations, cfg);
        if (cfg.isEmpty()) return template;
        return new ConfiguredTemplate<>(template, cfg);
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
    
    /**
     * Creates an initializing factory.
     * @param <Entity>
     * @param factory
     * @param initializers
     * @return initializing factory
     */
    public static <Entity> EntityFactory<Entity> initializingFactory(EntityFactory<Entity> factory, Collection<EntityInitializer<? super Entity>> initializers) {
        return buildFactory(b -> {
            b.set(factory);
            initializers.forEach(i -> b.add(i));
        });
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
    
    public static <Entity> EntityTemplate<Entity> asTemplate(EntityFactory<Entity> factory) {
        Objects.requireNonNull(factory, "factory");
        return new EntityTemplate<Entity>() {
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
    
    protected static final class MultiConfiguration<Entity> implements EntityConfiguration<Entity> {
        
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
    
    protected static class ConfiguredTemplate<Entity> implements EntityTemplate<Entity> {

        private final EntityTemplate<Entity> template;
        private final List<EntityConfiguration<? super Entity>> configurations;

        public ConfiguredTemplate(EntityTemplate<Entity> template, List<EntityConfiguration<? super Entity>> configurations) {
            this.template = template;
            this.configurations = configurations;
        }

        @Override
        public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
            FactoryBuilder<Entity> fb = builder.set(template, resultSet);
            for (EntityConfiguration<? super Entity> cfg: configurations) {
                fb.add(cfg, resultSet);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(template).append(" with ");
            configurations.stream().forEach(c -> {
                sb.append(c).append(", ");
            });
            sb.setLength(sb.length()-2);
            return sb.toString();
        }
    }
}
