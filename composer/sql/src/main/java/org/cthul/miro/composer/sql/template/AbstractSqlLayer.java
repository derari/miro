package org.cthul.miro.composer.sql.template;

import java.util.LinkedHashSet;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.composer.Configurable;
import org.cthul.miro.composer.Copyable;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.composer.impl.AbstractTemplateLayer;
import org.cthul.miro.composer.sql.SqlSnippet;
import org.cthul.miro.db.sql.SqlFilterableClause;

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
    protected abstract class ViewComposerBase implements MapNode<String, Configurable>, Copyable<Builder> {

        final LinkedHashSet<SqlSnippet<?>> snippets = new LinkedHashSet<>();
        final InternalComposer<? extends Builder> ic;
        final Composer dependencyComposer = new Composer() {
            @Override
            public void require(Object key) {
                dependencyRequire(key);
            }
            @Override
            public <V> V node(org.cthul.miro.util.Key<V> key) {
                return ic.node(key);
            }
        };

        public ViewComposerBase(InternalComposer<? extends Builder> ic) {
            this.ic = ic;
        }
        
        protected ViewComposerBase(InternalComposer<? extends Builder> ic, ViewComposerBase source) {
            this.ic = ic;
            snippets.addAll(source.snippets);
        }

//        @Override
//        public void addAttribute(String attributeKey) {
//            getSnippet(attributeKey);
//            SqlAttribute attribute = getOwner().getAttributes().get(attributeKey);
//            if (attributes.add(attribute)) {
//                attribute.requireDependencies(dependencyComposer);
//            }
//        }
        
        protected abstract SqlSnippet<? super Builder> getSnippet(String key);
        
        protected Configurable getSnippetPart(String key) {
            SqlSnippet<? super Builder> snippet = getSnippet(key);
            if (snippet == null) return null;
            if (snippets.add(snippet)) {
                snippet.requireDependencies(dependencyComposer);
                snippet.addTo(snippet, ic);
            }
            return ic.node(snippet);
        }

        @Override
        public Configurable get(String key) {
            int dot = key.indexOf('.');
            if (dot > 0) {
                JoinedView jv = getOwner().getJoinedViews().get(key.substring(0, dot));
                if (jv != null) {
                    key = key.substring(dot+1);
                    return ic.node(jv).get(key);
                }
            }
            return getSnippetPart(key);
        }
        
        protected void dependencyRequire(Object o) {
            if (o instanceof String) {
                getSnippet((String) o);
            } else {
                ic.require(o);
            }
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
