package org.cthul.miro.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ViewD<D> extends View {
    
    default D delete() {
        return delete(Collections.emptyList());
    }
    
    default D delete(Object... attributes) {
        return delete(Arrays.asList(attributes));
    }
    
    D delete(List<?> attributes);
}
