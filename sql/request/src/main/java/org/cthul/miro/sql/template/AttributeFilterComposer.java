package org.cthul.miro.sql.template;

/**
 *
 */
public interface AttributeFilterComposer {
  
    AttributeFilter getAttributeFilter();
//    
//    interface Delegator extends AttributeFilterComposer {
//        
//        AttributeFilterComposer getAttributeFilterComposerDelegate();
//
//        @Override
//        default AttributeFilter getAttributeFilter() {
//            return getAttributeFilterComposerDelegate().getAttributeFilter();
//        }
//    }
}
