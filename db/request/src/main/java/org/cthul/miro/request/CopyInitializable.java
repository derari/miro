package org.cthul.miro.request;

/**
 *
 */
public abstract class CopyInitializable<Composer> 
                implements Initializable<Composer>, Copyable2<Composer> {
    
    protected abstract Initializable<Composer> copyInstance();

    @Override
    public Object copy(Composer composer) {
        Initializable<Composer> copy = copyInstance();
        copy.initialize(composer);
        return copy;
    }
}
