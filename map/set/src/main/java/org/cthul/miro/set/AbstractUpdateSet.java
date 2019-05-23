package org.cthul.miro.set;

import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.db.request.MiUpdate;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.map.MappedUpdateRequest;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilterComposer;

/**
 *
 * @param <Entity>
 * @param <Cmp>
 * @param <This>
 */
public abstract class AbstractUpdateSet<
        Entity, 
        Cmp extends MappedUpdateRequest<Entity, ?> & PropertyFilterComposer,
        This extends AbstractUpdateSet<Entity, Cmp, This>> extends AbstractComposable<Cmp, This> {

    /*private*/ MiConnection cnn;
    private final RequestBuilder<Entity, ?, Cmp> requestBuilder;

    public AbstractUpdateSet(MiConnection cnn, RequestBuilder<Entity, ?, Cmp> requestBuilder) {
        this.cnn = cnn;
        this.requestBuilder = requestBuilder;
    }

    protected AbstractUpdateSet(AbstractUpdateSet<Entity, Cmp, This> source) {
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

//    protected This withRepository(Repository repository) {
//        return doSafe(me -> {
//            me.getComposer().getType().setRepository(repository);
//            if (repository instanceof MiConnection) {
//                me.cnn = (MiConnection) repository;
//            }
//        });
//    }
//
//    protected This withTemplate(EntityTemplate<Entity> template) {
//        return setUp(typeKey(), type -> type.setTemplate(template));
//    }

    @Override
    protected Cmp newComposer() {
        return requestBuilder.newComposer();
    }
    
    protected MiAction<Long> buildResult() {
        return requestBuilder.buildResult(cnn, getComposer());
    }
    
    private static <E> Function<MappedQueryComposer<E>, MappedQueryComposer.Type<E>> typeKey() {
        return (Function) TYPE;
    }
    
    private static final Function<MappedQueryComposer, MappedQueryComposer.Type> TYPE = MappedQueryComposer::getType;
    protected static final Function<PropertyFilterComposer, PropertyFilter> PROPERTY_FILTER = PropertyFilterSet.PROPERTY_FILTER;
    protected static final Function<MappedQueryComposer<?>, ListNode<String>> FETCHED_PROPERTIES = MappedQueryComposer::getFetchedProperties;
    
    protected static <Entity, Req extends MiUpdate, Cmp extends RequestComposer<Req>> RequestBuilder<Entity, Req, Cmp> request(RequestType<? extends Req> requestType, Supplier<? extends Cmp> newComposer) {
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
    
    public static interface RequestBuilder<Entity, Req extends MiUpdate, Cmp extends RequestComposer<Req>> {
        
        RequestType<? extends Req> getRequestType();
        
        Cmp newComposer();

        default MiAction<Long> buildResult(MiConnection cnn, Cmp composer) {
            Req request = cnn.newRequest(getRequestType());
            composer.build(request);
            return request.asAction();
        }
    }
}
