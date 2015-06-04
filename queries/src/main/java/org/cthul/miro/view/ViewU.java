package org.cthul.miro.view;

public interface ViewU<U> extends View {
    
    U update();
    
    U update(Object... attributes);
}
