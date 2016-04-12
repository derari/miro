package org.cthul.miro.composer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cthul.miro.composer.Copyable;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.TemplateLayer;

/**
 * A template that builds on `Builder`, but internally builds on `Adapted`.
 * @param <Builder>
 * @param <Adapted>
 */
public class AdaptedTemplate<Builder, Adapted> implements Template<Builder> {

    private final Map<InternalComposer<? extends Builder>, AdaptingComposer> cache = new WeakHashMap<>();
    private final Template<Adapted> template;
    private final Function<? super Builder, ? extends Adapted> adapter;

    public AdaptedTemplate(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        this.template = template;
        this.adapter = adapter;
    }

    protected AdaptingComposer adapt(InternalComposer<? extends Builder> query) {
        return new AdaptingComposer(query);
    }

    protected AdaptingComposer adaptedComposer(InternalComposer<? extends Builder> query) {
        AdaptingComposer ac = cache.get(query);
        if (ac == null) {
            ac = adapt(query);
            cache.put(query, ac);
        }
        return ac;
    }
    
    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> query) {
        template.addTo(key, adaptedComposer(query));
    }

    @Override
    public String toString() {
        return "{" + template;
    }
    
    /**
     * Converts a template that builds on `Adapted` into a template that builds on `Builder`. 
     * @param <Builder>
     * @param <Adapted>
     * @param template
     * @param adapter
     * @return adapted template
     */
    public static <Builder, Adapted> Template<Builder> adapt(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptedTemplate<Builder, Adapted>(template, adapter);
    }
    
//    /**
//     * Converts a composer that builds on `Builder` into a composer that builds on `Adapted`.
//     * @param <Builder>
//     * @param <Adapted>
//     * @param query
//     * @param adapter
//     * @return adapted composer
//     */
//    protected static <Builder, Adapted> InternalComposer<Adapted> adapt(InternalComposer<? extends Builder> query, Function<? super Builder, ? extends Adapted> adapter) {
//        return new AdaptingComposer<>(query, adapter);
//    }
    
    /**
     * Converts a template layer that builds on `Adapted` into a layer that builds on `Builder`.
     * @param <Builder>
     * @param <Adapted>
     * @param template
     * @param adapter
     * @return adapted layer
     */
    public static <Builder, Adapted> TemplateLayer<Builder> adapt(TemplateLayer<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        return new AdaptedLayer<>(template, adapter);
    }
    
    /**
     * A template layer that builds on `Builder`, but internally builds on `Adapter`.
     * @param <Adapted>
     * @param <Builder> 
     */
    public static class AdaptedLayer<Adapted, Builder> implements TemplateLayer<Builder> {
        
        private final TemplateLayer<Adapted> layer;
        private final Function<? super Builder, ? extends Adapted> adapter;

        public AdaptedLayer(TemplateLayer<Adapted> layer, Function<? super Builder, ? extends Adapted> adapter) {
            this.layer = layer;
            this.adapter = adapter;
        }

        @Override
        public <A extends Builder> Template<A> build(Template<? super A> parent) {
            ParentAdapter<A, Adapted> pAdapter = new ParentAdapter<>(parent);
            Template<Adapted> template = layer.build(pAdapter);
            return AdaptedTemplate.adapt(template, adapter);
        }

        @Override
        public String toString() {
            return "{" + layer + "}";
        }
    }
    
    public static interface ComposerWrapper {
        InternalComposer<?> getActual();
    }
    
    protected class AdaptingComposer extends Templates.InternalQueryComposerDelegator<Adapted> {
        
        private final InternalComposer<? extends Builder> composer;
        private List<StatementPart<? super Adapted>> parts = null;

        public AdaptingComposer(InternalComposer<? extends Builder> composer) {
            super(composer, null);
            this.composer = composer;
        }

        @Override
        public InternalComposer<?> getActual() {
            return composer;
        }
        
        private void initializeParts() {
            if (parts != null) return;
            class AdaptedParts implements StatementPart<Builder>, Copyable<Builder> {
                @Override
                public void addTo(Builder builder) {
                    Adapted adaptedBuilder = adapter.apply(builder);
                    parts.forEach(p -> p.addTo(adaptedBuilder));
                }
                @Override
                public Object copyFor(InternalComposer<Builder> ic2) {
                    AdaptingComposer ac2 = AdaptedTemplate.this.adaptedComposer(ic2);
                    CopyManager cm = ic2.node(CopyManager.key);
                    ac2.initializeParts();
                    ac2.parts.addAll(cm.copyAll(parts));
                    return null;
                }
                @Override
                public String toString() {
                    return parts.stream().map(Object::toString).collect(Collectors.joining(",", "{", "}"));
                }
            }
            parts = new ArrayList<>();
            composer.addPart(new AdaptedParts());
        }

        @Override
        public void addPart(StatementPart<? super Adapted> part) {
            if (parts == null) {
                initializeParts();
            }
            parts.add(part);
        }
    }
    
    /**
     * A template that builds on `Adapted`, but internally builds on `Builder`.
     * @param <Builder>
     * @param <Adapted> 
     */
    public static class ParentAdapter<Builder, Adapted> implements Template<Adapted> {
        private final Template<? super Builder> actual;

        public ParentAdapter(Template<? super Builder> actual) {
            this.actual = actual;
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Adapted> query) {
            ComposerWrapper adapter = (ComposerWrapper) query;
            actual.addTo(key, (InternalComposer) adapter.getActual());
        }

        @Override
        public String toString() {
            if (actual == Templates.noOp()) {
                return "}";
            }
            if (actual instanceof ParentAdapter) {
                return "}" + actual.toString();
            }
            return "}>" + actual.toString();
        }
    }    
}
