package org.cthul.miro.sql.template.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.request.*;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.SqlClause.BooleanExpression;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.sql.template.*;
import org.cthul.miro.sql.template.AttributeFilter.AttributeFilterKey;
import org.cthul.miro.sql.template.AttributeFilter.Comparative;
import org.cthul.miro.sql.template.impl.AttributeFilterPart.AttributesIn;

/**
 *
 */
public class AttributeFilterPart extends CopyableNodeSet<AttributeFilterKey, Void, AttributesIn> 
                implements AttributeFilter, Initializable<SqlDqmlComposer>, Copyable2<SqlDqmlComposer>, StatementPart<SqlFilterableClause> {
    
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
    
    protected class AttributesIn implements StatementPart<SqlFilterableClause>, ListNode<Object[]>, Copyable2<Object> {

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
        public boolean allowRead() {
            return true;
        }

        @Override
        public Object copy(Object composer) {
            return new AttributesIn(this);
        }
    }
    
}
