package org.cthul.miro.composer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.Template;

/**
 *
 * @param <Builder>
 * @param <Adapted>
 */
public abstract class AdaptedTemplate<Builder, Adapted> implements Template<Builder> {

    private final Map<InternalComposer<? extends Builder>, InternalComposer<? extends Adapted>> cache = new WeakHashMap<>();
    private final Template<Adapted> template;

    public AdaptedTemplate(Template<Adapted> template) {
        this.template = template;
    }
    
    protected abstract InternalComposer<? extends Adapted> adapt(InternalComposer<? extends Builder> query);

    protected InternalComposer<? extends Adapted> adaptedComposer(InternalComposer<? extends Builder> query) {
        InternalComposer<? extends Adapted> iqc = cache.get(query);
        if (iqc == null) {
            iqc = adapt(query);
            cache.put(query, iqc);
        }
        return iqc;
    }
    
    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> query) {
        template.addTo(key, adaptedComposer(query));
    }

    @Override
    public String toString() {
        return "{" + template;
    }
    
    public static <Builder, Adapted> Template<Builder> adapt(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptedTemplate<Builder, Adapted>(template) {
            @Override
            protected InternalComposer<? extends Adapted> adapt(InternalComposer<? extends Builder> query) {
                return query.adapt(adapter);
            }
        };
    }
    
    public static <Builder, Adapted> InternalComposer<Adapted> adapt(InternalComposer<? extends Builder> query, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptingComposer<>(query, adapter);
    }
    
    public static <Builder, Adapted> Function<? super Template<? super Builder>, ? extends Template<? super Builder>> adapt(Function<? super Template<? super Adapted>, ? extends Template<? super Adapted>> newTemplate, Function<? super Builder, ? extends Adapted> adapter) {
        return parent -> {
            Template<Adapted> adaptedParent = new AdaptedParent<>(parent);
            return newTemplate.apply(adaptedParent).adapt(adapter);
        };
    }
    
    public static class AdaptingComposer<Builder, Adapted> extends ComposerParts.InternalQueryComposerDelegator<Adapted> {
        
        private final InternalComposer<? extends Builder> composer;
        private final List<StatementPart<? super Adapted>> parts = new ArrayList<>();

        public AdaptingComposer(InternalComposer<? extends Builder> composer, Function<? super Builder, ? extends Adapted> adapter) {
            super(composer, null);
            this.composer = composer;
            StatementPart<Builder> queryPartAdapter = builder -> {
                Adapted adaptedBuilder = adapter.apply(builder);
                parts.forEach(p -> p.addTo(adaptedBuilder));
            };
            composer.addPart(queryPartAdapter);
        }

        public InternalComposer<? extends Builder> getActual() {
            return composer;
        }

        @Override
        public void addPart(StatementPart<? super Adapted> part) {
            parts.add(part);
        }
    }
    
    public static class AdaptedParent<Builder, Adapted> implements Template<Adapted> {
        private final Template<? super Builder> actual;

        public AdaptedParent(Template<? super Builder> actual) {
            this.actual = actual;
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Adapted> query) {
            AdaptingComposer<Builder, Adapted> adapter = (AdaptingComposer) query;
            actual.addTo(key, adapter.getActual());
        }

        @Override
        public String toString() {
            return "}" + actual.toString();
        }
    }    
}
