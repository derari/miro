package org.cthul.miro.view;

public interface ViewR<R> extends View {
    
    R select();
    
    R select(Object... attributes);
}
