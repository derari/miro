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
public final class CombinedEntitySetup<Entity> implements EntitySetup<Entity> {
    
    public static <Entity> EntitySetup<Entity> combine(EntitySetup<? super Entity>... setups) {
        return combine(Arrays.asList(setups));
    }
    
    public static <Entity> EntitySetup<Entity> combine(Collection<? extends EntitySetup<? super Entity>> setups) {
        if (setups.size() == 1) {
            return (EntitySetup<Entity>) setups.iterator().next();
        }
        final List<EntitySetup<? super Entity>> list = new ArrayList<>(setups.size());
        for (EntitySetup<? super Entity> s: setups) {
            addTo(s, list);
        }
        return new CombinedEntitySetup<>(list);
    }
    
    private static <Entity> void addTo(EntitySetup<? super Entity> setup, List<EntitySetup<? super Entity>> list) {
        if (setup instanceof CombinedEntitySetup) {
            list.addAll(Arrays.asList(((CombinedEntitySetup<Entity>) setup).setups));
        } else {
            list.add(setup);
        }
    }
    
    private final EntitySetup<? super Entity>[] setups;

    public CombinedEntitySetup(Collection<? extends EntitySetup<? super Entity>> setups) {
        this.setups = setups.toArray(new EntitySetup[setups.size()]);
    }

    @Override
    public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
        final EntityInitializer<? super Entity>[] inits = new EntityInitializer[setups.length];
        for (int i = 0; i < inits.length; i++) {
            inits[i] = setups[i].newInitializer(rs);
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