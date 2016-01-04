package org.cthul.miro.view;

import java.util.List;
import java.util.function.BiFunction;
import org.cthul.miro.composer.template.Template;

/**
 *
 */
public class ViewCRUDImpl<C, CB, R, U, D> implements ViewC<C> {
    
    private Template<? super CB> cTemplate;
    private BiFunction<List<?>, ? super Template<? super CB>, C> cFactory;

    @Override
    public C insert(Object... attributes) {
        List<?> list = null;
        return cFactory.apply(list, cTemplate);
    }
}
