package org.cthul.miro.sql.map;

import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.map.PropertyFilterComposer;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.ComposerState.Behavior;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.sql.composer.AttributeFilter;
import org.cthul.miro.sql.composer.SelectComposer;

public class MappedSelectComposerImpl<Entity, T extends SelectComposer.Delegator & MappedQueryComposer.Delegator<Entity>> 
        implements SelectComposer.Delegator, MappedQueryComposer.Internal<Entity>, MappedQueryComposer.Delegator<Entity>, Behavior<T> {

    public static <Entity> MappedSelectRequest<Entity> newComposer(MappedQueryComposer<Entity> mappedQueryComposer, SelectComposer selectComposer) {
        return ComposerState.builder()
                .setImpl(new MappedSelectComposerImpl<>())
                .putAdapted("getMappedQueryComposerDelegate", mappedQueryComposer, (MappedQuery q) -> q.getMapping())
                .putAdaptedNoOverride("getSelectComposerDelegate", selectComposer, (MappedQuery q) -> q.getStatement())
                .create(MappedSelectRequest.class);
    }

    protected MappedSelectComposerImpl() {
    }

    private T actual = null;

    @Override
    public Behavior<MappedQueryComposer.Delegator> copy() {
        return new MappedSelectComposerImpl();
    }

    @Override
    public void initialize(T composer) {
        actual = composer;
    }

    @Override
    public MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
        return actual.getMappedQueryComposerDelegate();
    }

    @Override
    public SelectComposer getSelectComposerDelegate() {
        return actual.getSelectComposerDelegate();
    }
    
    public SelectComposer getSelectComposer() {
        return actual.getSelectComposerDelegate();
    }

    @Override
    public PropertyFilterComposer getPropertyFilterComposerDelegate() {
        return getMappedQueryComposerDelegate();
    }

    @Override
    public ListNode<String> getSelectedAttributes() {
        return getSelectComposerDelegate().getSelectedAttributes();
    }

    @Override
    public AttributeFilter getAttributeFilter() {
        return getSelectComposerDelegate().getAttributeFilter();
    }
}
