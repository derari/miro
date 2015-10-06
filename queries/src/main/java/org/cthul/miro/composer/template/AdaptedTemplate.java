package org.cthul.miro.composer.template;

import java.util.function.Function;
import org.cthul.miro.composer.QueryPart;

/**
 *
 * @param <Builder>
 * @param <Adapted>
 */
public abstract class AdaptedTemplate<Builder, Adapted> implements Template<Builder> {

    private final Template<Adapted> template;

    public AdaptedTemplate(Template<Adapted> template) {
        this.template = template;
    }
    
    protected abstract Adapted adapt(Builder builder);

    @Override
    public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
        template.addTo(key, query.adapt(this::adapt));
    }
    
    public static <Builder, Adapted> Template<Builder> adapt(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        return (key, query) -> {
            template.addTo(key, query.adapt(adapter));
        };
    }
    
    public static <Builder, Adapted> InternalQueryComposer<Adapted> adapt(InternalQueryComposer<Builder> query, Function<? super Builder, ? extends Adapted> adapter) {
        return new InternalQueryComposer<Adapted>() {
            @Override
            public void addPart(Object key, QueryPart<? super Adapted> part) {
                query.addPart(key, new AdaptedPart<Builder, Adapted>(part) {
                    @Override
                    public void addTo(Builder builder) {
                        part.addTo(adapter.apply(builder));
                    }
                });
            }
            @Override
            public void put2(Object key, Object key2, Object... args) {
                query.put2(key, key2, args);
            }
        };
    }
    
    public static abstract class AdaptedPart<Builder, Adapted> implements QueryPart<Builder> {
        private final QueryPart<? super Adapted> part;

        public AdaptedPart(QueryPart<? super Adapted> part) {
            this.part = part;
        }

        @Override
        public void put(Object key, Object... args) {
            part.put(key, args);
        }

        @Override
        public void setUp(Object... args) {
            part.setUp(args);
        }
    }
}
