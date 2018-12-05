package org.cthul.miro.map;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.result.Results;
import org.cthul.miro.composer.RequestComposer;

/**
 *
 * @param <Entity>
 * @param <Req>
 */
public class MappedQuery<Entity, Req extends MiQuery> extends MappedStatement<Entity, Req> {

    public MappedQuery(Req statement) {
        super(statement);
    }

    public MappedQuery(MiConnection cnn, RequestType<Req> queryType) {
        this(cnn.newRequest(queryType));
    }
    
    public MappedQuery<Entity, Req> apply(RequestComposer<? super MappedQuery<Entity, Req>> builder) {
        builder.build(this);
        return this;
    }
    
    public Results.Action<Entity> result() {
        return getStatement().asAction()
                .andThen(Results.build(getEntityType()));
    }
}
