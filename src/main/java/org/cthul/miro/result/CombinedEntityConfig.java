package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.cthul.miro.util.Closables;

/**
 *
 */
public final class CombinedEntityConfig<Entity> implements EntityConfiguration<Entity> {
    
    public static <Entity> EntityConfiguration<Entity> combine(EntityConfiguration<? super Entity>... configs) {
        return combine(Arrays.asList(configs));
    }
    
    public static <Entity> EntityConfiguration<Entity> combine(Collection<? extends EntityConfiguration<? super Entity>> configs) {
        if (configs.size() == 1) {
            return (EntityConfiguration<Entity>) configs.iterator().next();
        }
        final List<EntityConfiguration<? super Entity>> list = new ArrayList<>(configs.size());
        for (EntityConfiguration<? super Entity> s: configs) {
            addTo(s, list);
        }
        return new CombinedEntityConfig<>(list);
    }
    
    private static <Entity> void addTo(EntityConfiguration<? super Entity> config, List<EntityConfiguration<? super Entity>> list) {
        if (config instanceof CombinedEntityConfig) {
            list.addAll(Arrays.asList(((CombinedEntityConfig<Entity>) config).configs));
        } else {
            list.add(config);
        }
    }
    
    private final EntityConfiguration<? super Entity>[] configs;

    public CombinedEntityConfig(Collection<? extends EntityConfiguration<? super Entity>> configs) {
        this.configs = configs.toArray(new EntityConfiguration[configs.size()]);
    }

    @Override
    public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
        final EntityInitializer<? super Entity>[] inits = new EntityInitializer[configs.length];
        for (int i = 0; i < inits.length; i++) {
            inits[i] = configs[i].newInitializer(rs);
        }
        return new CombinedInit<>(inits);
    }
    
    private static class CombinedInit<Entity> implements EntityInitializer<Entity> {
        
        private final EntityInitializer<? super Entity>[] inits;

        public CombinedInit(EntityInitializer<? super Entity>[] inits) {
            this.inits = inits;
        }

        @Override
        public void apply(Entity entity) throws SQLException {
            for (EntityInitializer<? super Entity> i: inits) {
                i.apply(entity);
            }
        }

        @Override
        public void complete() throws SQLException {
            for (EntityInitializer<? super Entity> i: inits) {
                i.complete();
            }
        }

        @Override
        public void close() throws SQLException {
            try {
                Closables.closeAll(inits);
            } catch (Exception e) {
                throw Closables.throwAs(e, SQLException.class);
            }
        }
    }
}