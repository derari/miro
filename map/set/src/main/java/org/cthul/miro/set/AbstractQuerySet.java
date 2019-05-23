package org.cthul.miro.set;

import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.EntityTemplate;
import org.cthul.miro.map.MappedQueryRequest;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilterComposer;
import org.cthul.miro.result.Results;

/**
 *
 * @param <Entity>
 * @param <Cmp>
 * @param <This>
 */
public abstract class AbstractQuerySet<
        Entity, 
        Cmp extends MappedQueryRequest<Entity, ?>,
        This extends AbstractQuerySet<Entity, Cmp, This>> extends AbstractComposable<Cmp, This> {

    /*private*/ MiConnection cnn;
    private final RequestBuilder<Entity, ?, Cmp> requestBuilder;

    public AbstractQuerySet(MiConnection cnn, RequestBuilder<Entity, ?, Cmp> requestBuilder) {
        this.cnn = cnn;
        this.requestBuilder = requestBuilder;
    }

    protected AbstractQuerySet(AbstractQuerySet<Entity, Cmp, This> source) {
        super(source);
        this.cnn = source.cnn;
        this.requestBuilder = source.requestBuilder;
    }
    
    protected This withFilterSet(PropertyFilterSet filterSet) {
        return setUp(PROPERTY_FILTER, filterSet::addFiltersTo);
    }
    
    protected This withValues(ValueSet<Entity, ?> valuesSet) {
        return (This) this;
    }
    
    protected This withConnection(MiConnection cnn) {
        return doSafe(me -> me.cnn = cnn);
    }

    protected This withRepository(Repository repository) {
        return doSafe(me -> {
            me.getComposer().getType().setRepository(repository);
            if (repository instanceof MiConnection) {
                me.cnn = (MiConnection) repository;
            }
        });
    }

    protected This withTemplate(EntityTemplate<Entity> template) {
        return setUp(typeKey(), type -> type.setTemplate(template));
    }

    @Override
    protected Cmp newComposer() {
        return requestBuilder.newComposer();
    }
    
    protected Results.Action<Entity> buildResult() {
        return requestBuilder.buildResult(cnn, getComposer());
    }
    
    private static <E> Function<MappedQueryComposer<E>, MappedQueryComposer.Type<E>> typeKey() {
        return (Function) TYPE;
    }
    
    private static final Function<MappedQueryComposer, MappedQueryComposer.Type> TYPE = MappedQueryComposer::getType;
    protected static final Function<PropertyFilterComposer, PropertyFilter> PROPERTY_FILTER = PropertyFilterSet.PROPERTY_FILTER;
    protected static final Function<MappedQueryComposer<?>, ListNode<String>> FETCHED_PROPERTIES = MappedQueryComposer::getFetchedProperties;
    
    protected static <Entity, Req extends MiQuery, Cmp extends RequestComposer<MappedQuery<Entity, ? extends Req>>> RequestBuilder<Entity, Req, Cmp> request(RequestType<? extends Req> requestType, Supplier<? extends Cmp> newComposer) {
        return new RequestBuilder<Entity, Req, Cmp>() {
            @Override
            public RequestType<? extends Req> getRequestType() {
                return requestType;
            }
            @Override
            public Cmp newComposer() {
                return newComposer.get();
            }
        };
    }
    
    public static interface RequestBuilder<Entity, Req extends MiQuery, Cmp extends RequestComposer<MappedQuery<Entity, ? extends Req>>> {
        
        RequestType<? extends Req> getRequestType();
        
        Cmp newComposer();

        default Results.Action<Entity> buildResult(MiConnection cnn, Cmp composer) {
            MappedQuery<Entity, ? extends Req> qry = new MappedQuery<>(cnn, getRequestType());
            return qry.apply(composer).result();
        }
    }
}
