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
    
    static <T> T copyRequest(T request) {
        return (T) ((RequestComposer) request).copy();
    }
    
    interface Delegator<Builder> extends RequestComposer<Builder> {
        
        RequestComposer<? super Builder> getRequestComposerDelegate();

        @Override
        default void build(Builder builder) {
            getRequestComposerDelegate().build(builder);
        }

        @Override
        default <Builder2> RequestComposer<Builder2> adapt(Function<? super Builder2, ? extends Builder> builderAdapter) {
            return getRequestComposerDelegate().adapt(builderAdapter);
        }
    }
}
