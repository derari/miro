package org.cthul.miro.request;

import java.util.function.Function;

/**
 *
 */
public class NewComposerState<This extends NewComposerState<This, Composer>, Composer> {
    
//    private Composer composer;
//    private This parent;
//
//    public NewComposerState(Composer composer, This parent) {
//        this.composer = composer;
//        this.parent = parent;
//    }
//    
//    protected <T> T inherit(Function<? super This, ? extends T> getter) {
//        This current = parent;
//        while (current != null) {
//            T value = getter.apply(current);
//            if (value != null) return value;
//            current = ((NewComposerState<This, Composer>) parent).parent;
//        }
//        return null;
//    }
//    
//    protected <T> T init(T object) {
//        return object;
//    }
//    
//    protected <T extends Initializable<? super Composer>> T init(T object) {
//        object.initialize(composer);
//        return object;
//    }
}
