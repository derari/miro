package org.cthul.miro.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ViewU<U> extends View {
    
    default U update() {
        return update(Collections.emptyList());
    }
    
    default U update(Object... attributes) {
        return update(Arrays.asList(attributes));
    }
    
    U update(List<?> attributes);
}
