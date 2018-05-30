package org.cthul.miro.sql.set;

import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.map.PropertyFilterComposer;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.ComposerState.Behavior;
import org.cthul.miro.sql.composer.SelectComposer;

public class MappedSelectRequestImpl<Entity> implements MappedSelectComposer.Delegator<Entity>, MappedSelectComposer.Internal<Entity>, Behavior<MappedSelectComposer.Delegator<Entity>> {

    public static <Entity> MappedSelectRequest<Entity> newComposer(MappedQueryComposer<Entity> mappedQueryComposer, SelectComposer selectComposer) {
        return ComposerState.builder()
                .setImpl(new MappedSelectRequestImpl<>())
                .addRequestInterface(MappedSelectRequest.class)
                .putAdapted("getMappedQueryComposerDelegate", mappedQueryComposer, (MappedQuery q) -> q.getMapping())
                .putAdapted("getSelectComposer", selectComposer, (MappedQuery q) -> q.getStatement())
                .create();
    }

    protected MappedSelectRequestImpl() {
    }

    private MappedSelectComposer.Delegator<Entity> actual = null;

    @Override
    public Behavior<MappedQueryComposer.Delegator> copy() {
        return new MappedSelectRequestImpl();
    }

    @Override
    public void initialize(MappedSelectComposer.Delegator<Entity> composer) {
        actual = composer;
    }

    @Override
    public MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
        return actual.getMappedQueryComposerDelegate();
    }

    @Override
    public SelectComposer getSelectComposer() {
        return actual.getSelectComposer();
    }

    @Override
    public PropertyFilterComposer getPropertyFilterComposerDelegate() {
        return getMappedQueryComposerDelegate();
    }
}
