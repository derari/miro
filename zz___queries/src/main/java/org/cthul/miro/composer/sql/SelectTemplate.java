package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.QueryComposerKey;
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
        switch (QueryComposerKey.key(key)) {
            case PHASE:
                return superPartType(QueryComposerKey.PHASE)
                        .andLink(SqlQueryKey.ATTRIBUTE);
        }
        switch (SqlQueryKey.key(key)) {
            case ATTRIBUTE:
                return newNodePart(SelectPart::new);
            case TABLE:
                return constNodePart(new FromPart());
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
    
    protected class SelectPart extends AbstractAttributeList implements QueryPart<SelectQueryBuilder> {

        public SelectPart(InternalQueryComposer<?> query) {
            super(query);
        }

        @Override
        public void addTo(SelectQueryBuilder builder) {
            getAttributes().forEach(at -> {
                at.writeSelectClause(
                        builder.select());
            });
        }
    }
}
