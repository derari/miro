package org.cthul.miro.sql.template;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.cthul.miro.request.impl.AbstractTemplateLayer;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractSqlLayer<Builder extends SqlFilterableClause> extends AbstractTemplateLayer<Builder> {
    
    private final SqlTemplates owner;

    public AbstractSqlLayer(SqlTemplates owner) {
        this.owner = owner;
    }

    public SqlTemplates getOwner() {
        return owner;
    }

//    @Override
//    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
//        switch (ComposerKey.key(key)) {
//            case PHASE:
//                return parent.andNode(ComposerView.phaseListener(mainView()));
//            case RESULT:
//                return parent.andLink(SqlQueryKey.ATTRIBUTE);
//            case FETCH_KEYS:
//                return parent.andSetUp(mainView(), cv -> cv.requireKeyAttributes());
//        }
//        switch (SqlQueryKey.key(key)) {
//            case ATTRIBUTE:
//                return ComposerView.resultListener(mainView());
//            case DEFAULT_ATTRIBUTES:
//                return Templates.setUp(mainView(), cv -> cv.requireDefaultAttributes());
//            case OPTIONAL_ATTRIBUTES:
//                return Templates.setUp(mainView(), cv -> cv.requireOptionalAttributes());
//            case FIND_BY_KEYS:
//                return Templates.newNodePart(FindByKeys::new);
//        }
//        Template<? super Builder> t = (Template) getOwner().getTemplates().get(key);
//        if (t != null) return t;
//        if (key instanceof String) {
//            String keyValue = (String) key;
//            int dot = keyValue.indexOf('.');
//            if (dot < 0) {
//                t = resolveStringKey(keyValue);
//                if (t != null) return t;
//            } else {
//                throw new UnsupportedOperationException();
//            }
//        }
//        return null;
//    }
    
//    protected Template<? super Builder> resolveStringKey(String key) {
//        SqlAttribute at = getOwner().getAttributes().get(key);
//        if (at != null) {
//            return Templates.setUp(mainView(), al -> al.addToResult(at));
//        }
//        SqlTable tb = getOwner().getTables().get(key);
//        if (tb != null) {
//            return Templates.setUp(mainView(), tl -> tl.add(tb));
//        }
//        return null;
//    }
//    
    protected abstract class ViewComposerBase implements ViewComposer, Copyable<Builder> {

        final Map<String, SnippetKey> snippetKeys;
        final InternalComposer<? extends Builder> ic;
        final Composer dependencyComposer = new Composer() {
            @Override
            public boolean include(Object key) {
                if (key instanceof String) {
                    return ViewComposerBase.this.get((String) key) != null;
                } else {
                    return ic.include(key);
                }
            }
            @Override
            public <V> V get(Key<V> key) {
                return ic.get(key);
            }
        };

        public ViewComposerBase(InternalComposer<? extends Builder> ic) {
            this.ic = ic;
            this.snippetKeys = new ConcurrentHashMap<>();
        }
        
        protected ViewComposerBase(InternalComposer<? extends Builder> ic, ViewComposerBase source) {
            this.ic = ic;
            this.snippetKeys = source.snippetKeys;
        }

        protected abstract Key<ViewComposer> getKey();
        
        protected abstract SqlSnippet<? super Builder> getSnippet(String key);
        
        protected void createSnippetPart(SnippetKey key) {
            SqlSnippet<? super Builder> snippet = getSnippet(key.getName());
            if (snippet == null) return;
            snippet.requireDependencies(dependencyComposer);
            snippet.addTo(key, ic);
        }

        @Override
        public void addSnippet(SnippetKey key) {
            String name = key.getName();
            int dot = name.indexOf('.');
            if (dot > 0) {
                JoinedView jv = getOwner().getJoinedViews().get(name.substring(0, dot));
                if (jv != null) {
                    name = name.substring(dot+1);
                    Configurable c = ic.node(jv.getViewKey()).get(name);
                    ic.addNode(key, c);
                }
            }
            createSnippetPart(key);
        }

        @Override
        public Configurable get(String key) {
            SnippetKey sk = snippetKeys.computeIfAbsent(key, k -> new SnippetKey(getKey(), k));
            return ic.node(sk);
        }

        @Override
        public SqlAttribute getAttribute(String key) {
            SqlAttribute at;
            int dot = key.indexOf('.');
            if (dot > 0) {
                JoinedView jv = getOwner().getJoinedViews().get(key.substring(0, dot));
                if (jv == null) return null;
                at = ic.node(jv.getViewKey()).getAttribute(key.substring(dot+1));
                return at.getWithPredix(key.substring(0, dot+1));
            } else {
                at = getOwner().getAttributes().get(key);
                at.getSelectSnippet().requireDependencies(dependencyComposer);
                return at;
            }
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
//    
//    protected class FindByKeys implements StatementPart<SqlFilterableClause>, ListNode<Object[]> {
//
//        private final List<Object[]> keys = new ArrayList<>();
//        
//        @Override
//        public void addTo(SqlFilterableClause builder) {
//            final int len = getOwner().getKeyAttributes().size();
//            SqlClause.Where<?> where = builder.where();
//            if (len == 1) {
//                where
//                    .ql(getOwner().getKeyAttributes().get(0).expression())
//                    .in().list(keys.stream().map(k -> k[0]));
//            } else {
//                SqlClause.Junction<?> j = where.either();
//                keys.forEach(k -> {
//                    SqlClause.Conjunction<?> c = j.or().all();
//                    for (int i = 0; i < len; i++) {
//                        c.ql(getOwner().getKeyAttributes().get(i).expression())
//                         .ql(" = ?")
//                         .pushArgument(k[i]);
//                    }
//                });
//            }
//        }
//
//        @Override
//        public void add(Object[] entry) {
//            this.keys.add(entry);
//        }
//    }
}
