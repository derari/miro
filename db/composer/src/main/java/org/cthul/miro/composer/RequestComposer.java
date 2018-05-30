package org.cthul.miro.composer;

import java.util.function.Function;

/**
 *
 * @param <Builder>
 */
public interface RequestComposer<Builder> {
    
    void build(Builder builder);
    
    RequestComposer<Builder> copy();
    
    <Builder2> RequestComposer<Builder2> adapt(Function<? super Builder2, ? extends Builder> builderAdapter);
}
