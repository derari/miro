package org.cthul.miro.request;

import java.util.function.Function;

/**
 *
 * @param <Builder>
 */
public interface RequestComposer2<Builder> {
    
    void build(Builder builder);
    
    RequestComposer2<Builder> copy();
    
    <Builder2> RequestComposer2<Builder> adapt(Function<? super Builder2, ? extends Builder> builderAdapter);
}
