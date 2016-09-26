package org.cthul.miro.sql.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.part.MapNode;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.SqlClause.BooleanExpression;
import org.cthul.miro.sql.template.AttributeFilter.AttributeFilterKey;
import org.cthul.miro.sql.template.AttributeFilter.Comparative;

/**
 * Maps the public {@link SqlComposerKey}s to internal {@link ViewComposer} methods.
 * @param <Builder>
 */
public class GeneralSqlLayer<Builder extends SqlFilterableClause> extends AbstractSqlLayer<Builder> {

    public GeneralSqlLayer(SqlTemplates owner) {
        super(owner);
    }

    @Override
    protected String getShortString() {
        return "SQL-misc";
    }
    
    @Override
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        switch (SqlComposerKey.key(key)) {
            case ATTRIBUTES:
                return ListNode.template(ic -> {
                    MapNode<String,?> cmp = ic.node(getOwner().getMainKey());
                    return cmp::get;
                });
            case SNIPPETS:
                return Templates.link(getOwner().getMainKey());
            case ATTRIBUTE_FILTER:
                return Templates.newNode(AttributeFilterHub::new);
        }
        if (key instanceof SnippetKey) {
            SnippetKey sk = (SnippetKey) key;
            return Templates.setUp(sk.getViewKey(), vc -> vc.addSnippet(sk));
        }
        if (key instanceof AttributeFilterKey) {
            return Templates.newNodePart(ic -> new AttributesIn(ic, ((AttributeFilterKey) key).getAttributeKeys()));
        }
        return null;
    }
    
    protected class AttributeFilterHub implements AttributeFilter, Copyable<Object> {
        final InternalComposer<?> ic;

        public AttributeFilterHub(InternalComposer<?> ic) {
            this.ic = ic;
        }

        @Override
        public ListNode<Object[]> forAttributes(String... attributeKeys) {
            return ic.node(new AttributeFilterKey(attributeKeys));
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            return new AttributeFilterHub(ic);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class AttributesIn implements StatementPart<SqlFilterableClause>, ListNode<Object[]>, Copyable<Object> {

        private final List<SqlAttribute> attributes;
        private final List<Object[]> values = new ArrayList<>();

        public AttributesIn(Composer c, String[] attributes) {
            ViewComposer vc = c.node(getOwner().getMainKey());
            this.attributes = Arrays.stream(attributes).map(vc::getAttribute).collect(Collectors.toList());
        }

        public AttributesIn(Composer c, AttributesIn source) {
            this.attributes = source.attributes;
            this.values.addAll(source.values);
        }
        
        @Override
        public void addTo(SqlFilterableClause sql) {
            if (attributes.size() == 1 && values.size() > 1) {
                if (Arrays.stream(values.get(0)).noneMatch(v -> v instanceof Comparative)) {
                    sql.where().ql(attributes.get(0).expression())
                            .in().list(values.stream().map(v -> v[0]));
                    return;
                }
            }
            if (values.size() == 1) {
                appendAttributeFilter(sql.where(), values.get(0));
            } else {
                SqlClause.Junction<?> junc = sql.where().either();
                values.forEach(v -> appendAttributeFilter(junc.or(), v));
            }
        }
        
        private void appendAttributeFilter(BooleanExpression<?> exp, Object[] valueTuple) {
            int len = attributes.size();
            if (len == 1) {
                exp.ql(attributes.get(0).expression()).ql(" ");
                AttributeFilter.appendComparative(valueTuple[0], exp);
            } else {
                SqlClause.Conjunction<?> conj = exp.all();
                for (int i = 0; i < len; i++) {
                    QlBuilder<?> ql = conj.and();
                    ql.ql(attributes.get(i).expression()).ql(" ");
                    AttributeFilter.appendComparative(valueTuple[i], ql);
                }
            }
        }

        @Override
        public void add(Object[] entry) {
            this.values.add(entry);
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            return new AttributesIn(ic, this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
}
