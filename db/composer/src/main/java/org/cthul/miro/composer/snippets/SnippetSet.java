package org.cthul.miro.composer.snippets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.composer.node.Configurable;
import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Copyable;

/**
 * @param <Builder>
 */
public class SnippetSet<Builder> {
    
    private final Map<Configurable.Key, Snippet<? super Builder>> snippets = new HashMap<>();
    private final Set<Object> onlyOnce = new HashSet<>();

    public SnippetSet() {
    }
    
    public SnippetSet<Builder> setUp(Configurable.Key key, Snippet<? super Builder> snippet) {
        snippets.put(key, snippet);
        return this;
    }
    
    public SnippetSet<Builder> setUp(Configurable.Key key, Consumer<? super Builder> snippet) {
        return setUp(key, new NoArgsSnippet<>(key, snippet));
    }
    
    public SnippetSet<Builder> setUp(String key, Snippet<? super Builder> snippet) {
        Configurable.Key cfgKey = Configurable.key(key);
        return setUp(cfgKey, snippet);
    }
    
    public SnippetSet<Builder> setUp(String key, Consumer<? super Builder> snippet) {
        Configurable.Key cfgKey = Configurable.key(key);
        return setUp(cfgKey, snippet);
    }
    
    public SnippetSet<Builder> once(Configurable.Key key, Snippet<? super Builder> snippet) {
        onlyOnce.add(key);
        return setUp(key, snippet);
    }
    
    public SnippetSet<Builder> once(Configurable.Key key, Consumer<? super Builder> snippet) {
        onlyOnce.add(key);
        return setUp(key, snippet);
    }
    
    public SnippetSet<Builder> once(String key, Snippet<? super Builder> snippet) {
        Configurable.Key cfgKey = Configurable.key(key);
        return once(cfgKey, snippet);
    }
    
    public SnippetSet<Builder> once(String key, Consumer<? super Builder> snippet) {
        Configurable.Key cfgKey = Configurable.key(key);
        return once(cfgKey, snippet);
    }
    
    public Snippets<Builder> newSnippetsPart() {
        return new SnippetsPart();
    }
//
//    @Override
//    protected Object getInitializationArg() {
//        return null;
//    }
//
//    @Override
//    protected Snippet<? super Builder> createPart(Configurable.Key key, Void hint) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

//    @Override
//    @SuppressWarnings("element-type-mismatch")
//    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
//        if (key == Snippets.key()) {
//            return Templates.newNodePart(() -> new SnippetsPart());
//        }
//        if (snippets.containsKey(key)) {
//            return Templates.newNode(ic -> new SnippetCfg((Configurable.Key) key, ic));
//        }
//        return null;
//    }
//
//    @Override
//    protected String getShortString() {
//        return "Snippets";
//    }
    
    protected class SnippetsPart implements Snippets<Builder>, StatementPart<Builder>, Copyable<Builder> {

        private final Set<Consumer<? super Builder>> actions = new LinkedHashSet<>();
        private final Map<Object, Consumer<? super Builder>> onceGuard = new HashMap<>();

        public SnippetsPart() {
        }

        protected SnippetsPart(SnippetsPart source) {
            this.actions.addAll(source.actions);
            this.onceGuard.putAll(source.onceGuard);
        }
        
        private Configurable.Key cfgKey(Object key) {
            if (key instanceof Configurable.Key) {
                return (Configurable.Key) key;
            }
            return Configurable.key(key);
        }

        @Override
        public void set(Object key, Object... args) {
            Configurable.Key cKey = cfgKey(key);
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
        public Configurable get(Object key) {
            return args -> set(key, args);
        }
        
        @Override
        public void addTo(Builder builder) {
            actions.forEach(c -> c.accept(builder));
        }

        @Override
        public Object copy(Builder composer) {
            return new SnippetsPart(this);
        }
    }
    
//    protected static class SnippetCfg implements Configurable, Copyable<Object> {
//        final Configurable.Key key;
//        final InternalComposer<?> ic;
//
//        public SnippetCfg(Configurable.Key key, InternalComposer<?> ic) {
//            this.key = key;
//            this.ic = ic;
//        }
//
//        @Override
//        public void set(Object... values) {
//            ic.node(Snippets.key()).set(key, values);
//        }
//
//        @Override
//        public Object copyFor(CopyComposer<Object> cc) {
//            return new SnippetCfg(key, ic.node(cc));
//        }
//
//        @Override
//        public boolean allowReadOnly(Predicate<Object> isLatest) {
//            return true;
//        }
//    }
    
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
    
    public static interface Snippet<Builder> {
        
        void accept(Builder builder, Object[] args);
        
        default Consumer<Builder> curry(Object[] args) {
            return builder -> accept(builder, args);
        }
    }
}
