package org.cthul.miro.composer;

import java.util.List;
import java.util.function.Function;

/**
 *
 */
public interface StatementFactory<Builder, Statement> {
    
    Statement create(Template<? super Builder> template, List<?> attributes);
    
    default Function<List<?>, Statement> withTemplate(Template<? super Builder> template) {
        return attributes -> create(template, attributes);
    }
}
