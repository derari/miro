package org.cthul.miro.view;

public interface ViewU<U> {
    
    U update();
    
    U update(String... attributes);
}
