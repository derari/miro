package org.cthul.miro.sql.composer.node;

import java.util.function.Function;
import org.cthul.miro.composer.AbstractComposer;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.composer.node.MapNode;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.composer.AttributeFilter;
import org.cthul.miro.sql.composer.SelectComposer;
import org.cthul.miro.sql.composer.SelectRequest;
import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.sql.composer.model.VirtualView;

public class DefaultSelectComposer<Builder> 
            extends AbstractComposer<Builder, SelectBuilder, SelectComposer> 
            implements SelectComposer {
    
    public static DefaultSelectComposer<SelectBuilder> create(SqlTemplates owner) {
        return new DefaultSelectComposer<>(owner, Function.identity());
    }
    
    public static SelectRequest createRequest(SqlTemplates owner) {
        return new Request(create(owner));
    }
    
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    protected static final KeyIndex INDEX = AbstractComposer.index();
    protected static final NodeKey SELECTED_ATTRIBUTES = INDEX.factory(SelectComposer::getSelectedAttributes);
    protected static final NodeKey VIEWS = INDEX.factory(SelectComposer::getViews);
    protected static final NodeKey ATTRIBUTE_FILTER = INDEX.factory(SelectComposer::getAttributeFilter);

    public DefaultSelectComposer(SqlTemplates owner, Function<? super Builder, ? extends SelectBuilder> adapter) {
        super(INDEX, new SelectNodeFactory(owner), adapter);
    }

    public DefaultSelectComposer(DefaultSelectComposer<?> src, Function<? super Builder, ? extends SelectBuilder> builderAdapter) {
        super(src, builderAdapter);
    }

    @Override
    protected Object copy(Function<?, ? extends SelectBuilder> builderAdapter) {
        return new DefaultSelectComposer/*<>*/(this, builderAdapter);
    }

    @Override
    public ListNode<String> getSelectedAttributes() {
        return getNode(SELECTED_ATTRIBUTES);
    }

    @Override
    public VirtualView getMainView() {
        return getRootComposer().getViews().get("");
    }

    @Override
    public MapNode<String, VirtualView> getViews() {
        return getNode(VIEWS);
    }

    @Override
    public AttributeFilter getAttributeFilter() {
        return getNode(ATTRIBUTE_FILTER);
    }

    protected static class Request 
            extends AbstractRequest<SelectQuery, Object> 
            implements SelectRequest, RequestComposer.Delegator<SelectQuery>, SelectComposer.Delegator {
        
        final DefaultSelectComposer<SelectBuilder> composer;

        public Request(DefaultSelectComposer<SelectBuilder> composer) {
            super(composer);
            this.composer = composer;
        }
        
        @Override
        public SelectComposer getSelectComposerDelegate() {
            return composer;
        }
        
        @Override
        public RequestComposer<SelectQuery> copy() {
            return new Request(composer._copy());
        }
    }
}
