package org.cthul.miro.view;

public interface ViewC<C> extends View {
    
    C insert();
    
    C insert(String... attributes);
}
