package org.cthul.miro.sql.set;

import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.sql.template.SelectComposer;

/**
 *
 */
public interface MappedSelectComposer<Entity> extends MappedQueryComposer<Entity>, SelectComposer {

    interface Internal<Entity> extends MappedSelectComposer<Entity>, MappedQueryComposer.Internal<Entity>, SelectComposer.Internal {
    }

    interface Delegator<Entity> extends MappedSelectComposer<Entity>, MappedQueryComposer.Delegator<Entity>, SelectComposer.Delegator {
    }
    
    

//    @Override
//    default Initializable<?> getAlways() {
//        return cmp -> {
//            getMappedQueryComposerInternalDelegate().getAlways().initialize(cmp);
////            getSelectComposerDelegate().get
//        };
//    }
}
