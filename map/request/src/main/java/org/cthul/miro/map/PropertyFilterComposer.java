package org.cthul.miro.map;

import org.cthul.miro.request.part.MultiKeyValueNode;

/**
 *
 */
public interface PropertyFilterComposer {
    
    PropertyFilter getPropertyFilter();
    
    interface Internal extends PropertyFilterComposer {
        
        MultiKeyValueNode<String, Object> getAttributeFilter();
    }
    
    interface Delegator extends PropertyFilterComposer {
        
        PropertyFilterComposer getPropertyFilterComposerDelegate();

        @Override
        default PropertyFilter getPropertyFilter() {
            return getPropertyFilterComposerDelegate().getPropertyFilter();
        }
    }
}
