package org.cthul.miro.view;

public interface ViewR<R> {
    
    R select();
    
    R select(String... attributes);
}
