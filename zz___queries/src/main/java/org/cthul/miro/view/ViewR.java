package org.cthul.miro.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ViewR<R> extends View {
    
    default R select() {
        return select(Collections.emptyList());
    }
    
    default R select(Object... attributes) {
        return select(Arrays.asList(attributes));
    }
    
    R select(List<?> attributes);
}
