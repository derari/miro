package org.cthul.miro.request.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.util.Key;

/**
 * A template that builds on `Builder`, but internally builds on `Adapted`.
 * @param <Builder>
 * @param <Adapted>
 */
public class AdaptedTemplate<Builder, Adapted> implements Template<Builder> {

    private final Key<AdaptingComposer> composerKey = new ValueKey<>("Adapt", true);
    private final Template<Adapted> template;
    private final Function<? super Builder, ? extends Adapted> adapter;

    public AdaptedTemplate(Template<Adapted> template, Function<? super Builder, ? extends Adapted> adapter) {
        this.template = template;
        this.adapter = adapter;
    }

    protected AdaptingComposer adapt(InternalComposer<? extends Builder> ic) {
        return new AdaptingComposer(ic);
    }
    
    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> ic) {
        if (key == composerKey) {
            ic.addNode(composerKey, adapt(ic));
            return;
        }
        AdaptingComposer ac = ic.node(composerKey);
        template.addTo(key, ac);
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
        return new AdaptedTemplate<>(template, adapter);
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
    
    protected class AdaptingComposer 
                    extends Templates.InternalQueryComposerDelegator<Adapted> 
                    implements Copyable<Builder> {
        
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
            setParts(new ArrayList<>());
        }

        private void setParts(List<StatementPart<? super Adapted>> parts) {
            class AdaptedParts implements StatementPart<Builder>, Copyable<Builder> {
                @Override
                public void addTo(Builder builder) {
                    Adapted adaptedBuilder = adapter.apply(builder);
                    parts.forEach(p -> p.addTo(adaptedBuilder));
                }
                @Override
                public Object copyFor(InternalComposer<Builder> ic2) {
                    ic2.node(composerKey); // initializes the copy of this part
                    return null;
                }
                @Override
                public boolean allowReadOnly(Predicate<Object> isLatest) {
                    return isLatest.test(AdaptingComposer.this);
                }
                @Override
                public String toString() {
                    return parts.stream().map(Object::toString).collect(Collectors.joining(",", "{", "}"));
                }
            }
            this.parts = parts;
            composer.addPart(new AdaptedParts());
        }

        @Override
        public void addPart(StatementPart<? super Adapted> part) {
            if (parts == null) {
                initializeParts();
            }
            parts.add(part);
        }

        @Override
        public Object copyFor(InternalComposer<Builder> ic) {
            AdaptingComposer ac2 = adapt(ic);
            if (parts != null) {
                ac2.initializeParts();
                parts.forEach(p -> {
                    StatementPart<? super Adapted> c = Copyable.tryCopy(p, this);
                    if (c != null) ac2.parts.add(c);
                });
            }
            return ac2;
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            if (parts == null) return true;
            return parts.stream().allMatch(isLatest);
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
