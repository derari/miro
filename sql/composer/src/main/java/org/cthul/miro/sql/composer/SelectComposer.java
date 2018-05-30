package org.cthul.miro.sql.composer;

import org.cthul.miro.sql.composer.model.VirtualView;
import org.cthul.miro.composer.node.ListNode;

/**
 *
 */
public interface SelectComposer extends SqlDqmlComposer, AttributeFilterComposer {
    
    ListNode<String> getSelectedAttributes();
    
    interface Internal extends SelectComposer {
        
    }
    
    interface Delegator extends SelectComposer {
        
        SelectComposer getSelectComposerDelegate();

        @Override
        default ListNode<String> getSelectedAttributes() {
            return getSelectComposerDelegate().getSelectedAttributes();
        }

        @Override
        default VirtualView getMainView() {
            return getSelectComposerDelegate().getMainView();
        }

        @Override
        default AttributeFilter getAttributeFilter() {
            return getSelectComposerDelegate().getAttributeFilter();
        }
    }
}
