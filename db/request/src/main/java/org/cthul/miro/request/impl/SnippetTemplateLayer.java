package org.cthul.miro.request.impl;

import org.cthul.miro.request.template.Snippets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.Snippets.Snippet;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.part.Parameterized;

/**
 * @param <Builder>
 */
public class SnippetTemplateLayer<Builder> extends AbstractTemplateLayer<Builder> {
    
    private final Map<Parameterized.Key, Snippet<? super Builder>> snippets = new HashMap<>();
    private final Set<Object> onlyOnce = new HashSet<>();
    
    public SnippetTemplateLayer<Builder> setUp(Parameterized.Key key, Snippet<? super Builder> snippet) {
        snippets.put(key, snippet);
        return this;
    }
    
    public SnippetTemplateLayer<Builder> setUp(Parameterized.Key key, Consumer<? super Builder> snippet) {
        snippets.put(key, new NoArgsSnippet<>(key, snippet));
        return this;
    }
    
    public SnippetTemplateLayer<Builder> setUp(String key, Snippet<? super Builder> snippet) {
        Parameterized.Key cfgKey = Parameterized.key(key);
        return setUp(cfgKey, snippet);
    }
    
    public SnippetTemplateLayer<Builder> setUp(String key, Consumer<? super Builder> snippet) {
        Parameterized.Key cfgKey = Parameterized.key(key);
        return setUp(cfgKey, snippet);
    }
    
    public SnippetTemplateLayer<Builder> once(Parameterized.Key key, Snippet<? super Builder> snippet) {
        onlyOnce.add(key);
        return setUp(key, snippet);
    }
    
    public SnippetTemplateLayer<Builder> once(Parameterized.Key key, Consumer<? super Builder> snippet) {
        onlyOnce.add(key);
        return setUp(key, snippet);
    }
    
    public SnippetTemplateLayer<Builder> once(String key, Snippet<? super Builder> snippet) {
        Parameterized.Key cfgKey = Parameterized.key(key);
        return once(cfgKey, snippet);
    }
    
    public SnippetTemplateLayer<Builder> once(String key, Consumer<? super Builder> snippet) {
        Parameterized.Key cfgKey = Parameterized.key(key);
        return once(cfgKey, snippet);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        if (key == Snippets.key()) {
            return Templates.newNodePart(() -> new SnippetsPart());
        }
        if (snippets.containsKey(key)) {
            return Templates.newNode(ic -> new SnippetCfg((Parameterized.Key) key, ic));
        }
        return null;
    }

    @Override
    protected String getShortString() {
        return "Snippets";
    }
    
    protected class SnippetsPart implements Snippets<Builder>, StatementPart<Builder>, Copyable {

        private final Set<Consumer<? super Builder>> actions = new LinkedHashSet<>();
        private final Map<Object, Consumer<? super Builder>> onceGuard = new HashMap<>();

        public SnippetsPart() {
        }

        protected SnippetsPart(SnippetsPart source) {
            this.actions.addAll(source.actions);
            this.onceGuard.putAll(source.onceGuard);
        }
        
        private Parameterized.Key cfgKey(Object key) {
            if (key instanceof Parameterized.Key) {
                return (Parameterized.Key) key;
            }
            return Parameterized.key(key);
        }

        @Override
        public void set(Object key, Object... args) {
            Parameterized.Key cKey = cfgKey(key);
            Snippet<? super Builder> snippet = snippets.get(cKey);
            if (snippet == null) {
                throw new IllegalArgumentException("Unknown snippet key: " + key);
            }
            if (onlyOnce.contains(cKey)) {
                once(cKey, snippet.curry(args));
            } else {
                add(snippet.curry(args));
            }
        }

        @Override
        public void once(Object key, Consumer<? super Builder> action) {
            Consumer<? super Builder> old = onceGuard.put(key, action);
            if (old != null) actions.remove(old);
            actions.add(action);
        }

        @Override
        public void add(Consumer<? super Builder> action) {
            actions.add(action);
        }
        
        @Override
        public void addTo(Builder builder) {
            actions.forEach(c -> c.accept(builder));
        }

        @Override
        public Object copyFor(CopyComposer cc) {
            return new SnippetsPart(this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected static class SnippetCfg implements Parameterized, Copyable {
        final Parameterized.Key key;
        final Snippets<?> snippets;

        public SnippetCfg(Parameterized.Key key, Composer ic) {
            this.key = key;
            this.snippets = ic.node(Snippets.key());
        }

        @Override
        public void set(Object... values) {
            snippets.set(key, values);
        }

        @Override
        public Object copyFor(CopyComposer cc) {
            return new SnippetCfg(key, cc);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    private static class NoArgsSnippet<Builder> implements Snippet<Builder> {
        final Object key;
        final Consumer<Builder> action;

        public NoArgsSnippet(Object key, Consumer<Builder> action) {
            this.key = key;
            this.action = action;
        }

        @Override
        public void accept(Builder builder, Object[] args) {
            curry(args).accept(builder);
        }

        @Override
        public Consumer<Builder> curry(Object[] args) {
            if (args != null && args.length > 0) {
                throw new IllegalArgumentException(key + 
                        ": No arguments expected, got " + Arrays.toString(args));
            }
            return action;
        }
    }
}
