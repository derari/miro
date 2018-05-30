package org.cthul.miro.map;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.result.Results;
import org.cthul.miro.composer.RequestComposer;

/**
 *
 * @param <Entity>
 * @param <Stmt>
 */
public class MappedQuery<Entity, Stmt extends MiQuery> extends MappedStatement<Entity, Stmt> {

    public MappedQuery(Stmt statement) {
        super(statement);
    }

    public MappedQuery(MiConnection cnn, RequestType<Stmt> queryType) {
        this(cnn.newStatement(queryType));
    }
    
    public Results.Action<Entity> query(RequestComposer<? super MappedQuery<Entity, Stmt>> builder) {
        builder.build(this);
        return getStatement().asAction()
                .andThen(Results.build(getEntityType()));
    }
}