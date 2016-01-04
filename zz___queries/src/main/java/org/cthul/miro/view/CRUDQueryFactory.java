package org.cthul.miro.view;

import java.util.List;
import org.cthul.miro.composer.template.Template;

/**
 *
 */
public interface CRUDQueryFactory<C, R, U, D, CBuilder, RBuilder, UBuilder, DBuilder> {
    
    Template<? super CBuilder> templateCreate();
    
    C create(Template<? super CBuilder> template, List<?> attributes);
    
}
