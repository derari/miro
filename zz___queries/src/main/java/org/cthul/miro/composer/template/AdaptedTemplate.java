package org.cthul.miro.composer.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import org.cthul.miro.composer.QueryPart;
import org.cthul.miro.composer.QueryParts;

/**
 *
 * @param <Builder>
 * @param <Adapted>
 */
public abstract class AdaptedTemplate<Builder, Adapted> implements Template<Builder> {

    private final Map<InternalQueryComposer<? extends Builder>, InternalQueryComposer<? extends Adapted>> cache = new WeakHashMap<>();
    private final Template<Adapted> template;

    public AdaptedTemplate(Template<Adapted> template) {
        this.template = template;
    }
    
    protected abstract InternalQueryComposer<? extends Adapted> adapt(InternalQueryComposer<? extends Builder> query);

    protected InternalQueryComposer<? extends Adapted> adaptedComposer(InternalQueryComposer<? extends Builder> query) {
        InternalQueryComposer<? extends Adapted> iqc = cache.get(query);
        if (iqc == null) {
            iqc = adapt(query);
            cache.put(query, iqc);
        }
        return iqc;
    }
    
    @Override
    public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
        template.addTo(key, adaptedComposer(query));
    }
    
    public static <Builder, Adapted> Template<Builder> adapt(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptedTemplate<Builder, Adapted>(template) {
            @Override
            protected InternalQueryComposer<? extends Adapted> adapt(InternalQueryComposer<? extends Builder> query) {
                return query.adapt(adapter);
            }
        };
    }
    
    public static <Builder, Adapted> InternalQueryComposer<Adapted> adapt(InternalQueryComposer<? extends Builder> query, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptingQuery<>(query, adapter);
    }
    
    public static class AdaptingQuery<Builder, Adapted> extends QueryParts.InternalQueryComposerDelegator<Adapted> {
        
        private final List<QueryPart<? super Adapted>> parts = new ArrayList<>();

        public AdaptingQuery(InternalQueryComposer<? extends Builder> query, Function<? super Builder, ? extends Adapted> adapter) {
            super(query, null);
            QueryPart<Builder> queryPartAdapter = builder -> {
                Adapted adaptedBuilder = adapter.apply(builder);
                parts.forEach(p -> p.addTo(adaptedBuilder));
            };
            query.addPart(queryPartAdapter);
        }

        @Override
        public void addPart(QueryPart<? super Adapted> part) {
            parts.add(part);
        }
    }
    
}
