package org.cthul.miro.sql.composer.node;

import org.cthul.miro.composer.CopyableNodeSet;
import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Copyable;
import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.sql.composer.SqlDqmlComposer;
import org.cthul.miro.sql.composer.model.SqlAttribute;
import org.cthul.miro.sql.composer.AttributeFilter;
import org.cthul.miro.sql.composer.model.VirtualView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.SqlClause.BooleanExpression;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.sql.composer.AttributeFilter.AttributeFilterKey;
import org.cthul.miro.sql.composer.node.AttributeFilterPart.AttributesIn;
import org.cthul.miro.sql.composer.Comparison;

/**
 *
 */
public class AttributeFilterPart extends CopyableNodeSet<AttributeFilterKey, Void, AttributesIn> 
                implements AttributeFilter, Initializable<SqlDqmlComposer>, Copyable<SqlDqmlComposer>, StatementPart<SqlFilterableClause> {
    
    private final SqlTemplates owner;
    private SqlDqmlComposer composer;

    public AttributeFilterPart(SqlTemplates owner) {
        this.owner = owner;
    }

    public AttributeFilterPart(AttributeFilterPart src, SqlDqmlComposer composer) {
        super(src);
        this.owner = src.owner;
        this.composer = composer;
    }

    @Override
    public void initialize(SqlDqmlComposer composer) {
        this.composer = composer;
    }

    @Override
    public Object copy(SqlDqmlComposer composer) {
        return new AttributeFilterPart(this, composer);
    }

    @Override
    protected Object getInitializationArg() {
        return null;
    }

    @Override
    protected void newEntry(AttributeFilterKey key, Void hint) {
        putNode(key, new AttributesIn(composer, key.getAttributeKeys()));
    }

    @Override
    public ListNode<Object[]> forAttributes(String... attributeKeys) {
        return getValue(new AttributeFilterKey(attributeKeys), null);
    }

    @Override
    public void addTo(SqlFilterableClause builder) {
        addPartsTo(builder);
    }
    
    protected class AttributesIn implements StatementPart<SqlFilterableClause>, ListNode<Object[]>, Copyable<Object> {

        private final List<SqlAttribute> attributes;
        private final List<Object[]> values = new ArrayList<>();

        public AttributesIn(SqlDqmlComposer composer, String[] attributes) {
            VirtualView vc = composer.getMainView();
            this.attributes = Arrays.stream(attributes).map(vc::getAttribute).collect(Collectors.toList());
        }

        public AttributesIn(AttributesIn source) {
            this.attributes = source.attributes;
            this.values.addAll(source.values);
        }
        
        @Override
        public void addTo(SqlFilterableClause sql) {
            if (attributes.size() == 1 && values.size() > 1) {
                if (Arrays.stream(values.get(0)).noneMatch(v -> v instanceof Comparison)) {
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
                Comparison.appendTo(valueTuple[0], exp);
            } else {
                SqlClause.Conjunction<?> conj = exp.all();
                for (int i = 0; i < len; i++) {
                    QlBuilder<?> ql = conj.and();
                    ql.ql(attributes.get(i).expression()).ql(" ");
                    Comparison.appendTo(valueTuple[i], ql);
                }
            }
        }

        @Override
        public void add(Object[] entry) {
            this.values.add(entry);
        }

        @Override
        public boolean allowReadOriginal() {
            return true;
        }

        @Override
        public Object copy(Object composer) {
            return new AttributesIn(this);
        }
    }
    
}
