package org.cthul.miro.view;

public interface ViewC<C> {
    
    C insert();
    
    C insert(String... attributes);
}
