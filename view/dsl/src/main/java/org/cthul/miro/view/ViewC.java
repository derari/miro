package org.cthul.miro.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ViewC<C> extends View {
    
    default C insert() {
        return insert(Collections.emptyList());
    }
    
    default C insert(Object... attributes) {
        return insert(Arrays.asList(attributes));
    }
    
    C insert(List<?> attributes);
}
