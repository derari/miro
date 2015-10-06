package org.cthul.miro.composer.sql;

import java.util.LinkedHashSet;
import org.cthul.miro.composer.QueryKey;
import org.cthul.miro.composer.QueryPart;
import static org.cthul.miro.composer.QueryParts.*;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;

/**
 *
 */
public class SelectTemplate extends SqlTemplate<SelectQueryBuilder> {

    public SelectTemplate(SqlTemplateBuilder owner, Template<? super SelectQueryBuilder> parent) {
        super(owner, parent);
    }

    @Override
    protected Template<? super SelectQueryBuilder> createPartType(Object key) {
        switch (QueryKey.key(key)) {
            case PHASE:
                return superPartType(key)
                        .andProxy(SqlQueryKey.ATTRIBUTE);
        }
        switch (SqlQueryKey.key(key)) {
            case ATTRIBUTE:
                return createPart(q -> new SelectPart(q));
            case TABLE:
                return constPart(new FromPart());
        }
        return super.createPartType(key);
    }
    
    protected class FromPart implements QueryPart<SelectQueryBuilder> {
        @Override
        public void addTo(SelectQueryBuilder builder) {
            getOwner().getMainTable().appendTo(
                    builder.from());
        }
    }
    
    protected class SelectPart implements QueryPart<SelectQueryBuilder> {

        private final LinkedHashSet<Attribute> attributes = new LinkedHashSet<>();
        private final InternalQueryComposer<? extends SelectQueryBuilder> query;

        public SelectPart(InternalQueryComposer<? extends SelectQueryBuilder> query) {
            this.query = query;
        }

        @Override
        public void put(Object key, Object... args) {
            if (key == QueryKey.Phase.COMPOSE) {
                if (attributes.isEmpty()) {
                    query.require(SqlQueryKey.DEFAULT_ATTRIBUTES);
                }
            } else {
                QueryPart.super.put(key, args);
            }
        }
        
        @Override
        public void setUp(Object... args) {
            for (Object a: args) {
                Attribute at = asAttribute(a);
                if (at == null) {
                    throw new IllegalArgumentException(String.valueOf(a));
                }
                attributes.add(at);
            }
        }

        @SuppressWarnings("element-type-mismatch")
        private Attribute asAttribute(Object a) {
            Attribute at;
            if (a instanceof Attribute) {
                at = (Attribute) a;
            } else {
                at = getOwner().getAttributes().get(a);
            }
            return at;
        }

        @Override
        public void addTo(SelectQueryBuilder builder) {
            attributes.forEach(at -> {
                at.writeSelectClause(
                        builder.select());
            });
        }
    }
}
