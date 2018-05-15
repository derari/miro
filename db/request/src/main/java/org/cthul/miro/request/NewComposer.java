package org.cthul.miro.request;

/**
 *
 */
public interface NewComposer<Context, Builder> extends RequestComposer<Builder> {
    //Initializable<Context>, Copyable2<Context>, 
    
    @Override
    NewComposer<Context, Builder> copy();
}
