package org.cthul.miro.map.node;

import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.composer.AbstractComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.entity.EntityTemplate;
import org.cthul.miro.map.AbstractQueryableType;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.PropertyFilter;

public class DefaultMappedQueryComposer<Entity, Builder> 
        extends AbstractComposer<Builder, Mapping<Entity>, MappedQueryComposer<Entity>> 
        implements MappedQueryComposer<Entity> {
    
    public static <Entity> DefaultMappedQueryComposer<Entity, Mapping<Entity>> create(AbstractQueryableType<Entity, ?> owner) {
        return new DefaultMappedQueryComposer<>(owner, Function.identity());
    }
    
    public static <Entity> DefaultMappedQueryComposer<Entity, Mapping<Entity>> create(AbstractQueryableType<Entity, ?> owner, Object typeKey, Supplier<? extends EntityTemplate<Entity>> defaultType) {
        return new DefaultMappedQueryComposer<>(owner, typeKey, defaultType, Function.identity());
    }

    protected static final KeyIndex INDEX = AbstractComposer.newIndex();
    protected static final NodeKey TYPE = INDEX.factory(MappedQueryComposer<?>::getType);
    protected static final NodeKey CONFIGURATION = INDEX.factory(MappedQueryComposer<?>::getConfiguration);
    protected static final NodeKey LOADED_PROPERTIES = INDEX.factory(MappedQueryComposer<?>::getLoadedProperties);
    protected static final NodeKey INCLUDED_PROPERTIES = INDEX.factory(MappedQueryComposer<?>::getIncludedProperties);
    protected static final NodeKey FETCHED_PROPERTIES = INDEX.factory(MappedQueryComposer<?>::getFetchedProperties);
    protected static final NodeKey SET_PROPERTIES = INDEX.factory(MappedQueryComposer<?>::getSetProperties);
    protected static final NodeKey PROPERTIES_FILTER = INDEX.factory(MappedQueryComposer<?>::getPropertyFilter);

    public DefaultMappedQueryComposer(AbstractQueryableType<Entity, ?> owner, Function<? super Builder, ? extends Mapping<Entity>> builderAdapter) {
        super(INDEX, new MappedQueryNodeFactory<>(owner), builderAdapter);
    }

    public DefaultMappedQueryComposer(AbstractQueryableType<Entity, ?> owner, Object typeKey, Supplier<? extends EntityTemplate<Entity>> defaultType, Function<? super Builder, ? extends Mapping<Entity>> builderAdapter) {
        super(INDEX, new MappedQueryNodeFactory<>(owner, typeKey, defaultType), builderAdapter);
    }

    protected DefaultMappedQueryComposer(DefaultMappedQueryComposer<Entity, ?> src, Function<? super Builder, ? extends Mapping<Entity>> builderAdapter) {
        super(src, builderAdapter);
    }

    @Override
    protected Object copy(Function<?, ? extends Mapping<Entity>> builderAdapter) {
        return new DefaultMappedQueryComposer/*<>*/(this, builderAdapter);
    }

    @Override
    protected void beforeBuild() {
        super.beforeBuild();
        getType();
    }

    @Override
    public Type<Entity> getType() {
        return getNode(TYPE);
    }

    @Override
    public Configuration<Entity> getConfiguration() {
        return getNode(CONFIGURATION);
    }

    @Override
    public ListNode<String> getLoadedProperties() {
        return getNode(LOADED_PROPERTIES);
    }

    @Override
    public ListNode<String> getIncludedProperties() {
        return getNode(INCLUDED_PROPERTIES);
    }

    @Override
    public ListNode<String> getFetchedProperties() {
        return getNode(FETCHED_PROPERTIES);
    }

    @Override
    public SetProperties getSetProperties() {
        return getNode(SET_PROPERTIES);
    }

    @Override
    public PropertyFilter getPropertyFilter() {
        return getNode(PROPERTIES_FILTER);
    }
}
