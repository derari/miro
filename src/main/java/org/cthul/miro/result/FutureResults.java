package org.cthul.miro.result;

import java.util.List;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.cursor.*;
import org.cthul.miro.util.FutureDelegator;

public class FutureResults<Entity> extends FutureDelegator<Results<Entity>> {

    public FutureResults(MiFuture<Results<Entity>> delegatee) {
        super(delegatee);
    }
    
    public MiFuture<List<Entity>> asList() {
        return onComplete(new MiFutureAction<MiFuture<Results<Entity>>, List<Entity>>() {
            @Override
            public List<Entity> call(MiFuture<Results<Entity>> f) throws Exception {
                return f.get().asList();
            }
        });
    }
    
    public MiFuture<Entity[]> asArray() {
        return onComplete(new MiFutureAction<MiFuture<Results<Entity>>, Entity[]>() {
            @Override
            public Entity[] call(MiFuture<Results<Entity>> f) throws Exception {
                return f.get().asArray();
            }
        });
    }
    
    public FutureCursor<Entity> asCursor() {
        MiFuture<ResultCursor<Entity>> f = onComplete(new MiFutureAction<MiFuture<Results<Entity>>, ResultCursor<Entity>>() {
            @Override
            public ResultCursor<Entity> call(MiFuture<Results<Entity>> f) throws Exception {
                return f.get().asCursor();
            }
        });
        return new FutureCursorDelegator<>(f);
    }
    
    public MiFuture<Entity> getFirst() {
        return onComplete(new MiFutureAction<MiFuture<Results<Entity>>, Entity>() {
            @Override
            public Entity call(MiFuture<Results<Entity>> f) throws Exception {
                return f.get().getFirst();
            }
        });
    }
    
    public MiFuture<Entity> getSingle() {
        return onComplete(new MiFutureAction<MiFuture<Results<Entity>>, Entity>() {
            @Override
            public Entity call(MiFuture<Results<Entity>> f) throws Exception {
                return f.get().getSingle();
            }
        });
    }
}
