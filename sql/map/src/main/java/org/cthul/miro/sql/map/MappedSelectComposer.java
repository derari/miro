package org.cthul.miro.sql.map;

import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.composer.node.MultiKeyValueNode;
import org.cthul.miro.sql.composer.SelectComposer;

/**
 *
 */
public interface MappedSelectComposer<Entity> extends MappedQueryComposer<Entity> {

    SelectComposer getSelectComposer();

    interface Internal<Entity> extends MappedSelectComposer<Entity>, MappedQueryComposer.Internal<Entity> {

        @Override
        default ListNode<String> getSelectedAttributes() {
            return getSelectComposer().getSelectedAttributes();
        }

        @Override
        default MultiKeyValueNode<String, Object> getAttributeFilter() {
            return getSelectComposer().getAttributeFilter();
        }
    }

    interface Delegator<Entity> extends MappedSelectComposer.Internal<Entity>, MappedQueryComposer.Delegator<Entity> {

        MappedSelectComposer<Entity> getMappedSelectComposerDelegate();
        
        @Override
        default SelectComposer getSelectComposer() {
            return getMappedSelectComposerDelegate().getSelectComposer();
        }

        @Override
        public default MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
            return getMappedSelectComposerDelegate();
        }
    }
}
