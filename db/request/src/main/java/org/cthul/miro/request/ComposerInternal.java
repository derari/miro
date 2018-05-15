package org.cthul.miro.request;

/**
 *
 */
public interface ComposerInternal {
    
//    default PartList<Builder> getPartList() {
//        throw new UnsupportedOperationException("Not implemented by factory");
//    }

    Initializable<?> getAlways();
    
//    interface PartList<Builder> {
//        
//        <Builder2> PartList<Builder2> adapt(Function<? super Builder, Builder2> adapter);
//        
//        void add(StatementPart<? super Builder> part);
//    }
}
