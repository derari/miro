package org.cthul.miro.composer.sql;

import static org.cthul.miro.composer.ComposerParts.*;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;

/**
 *
 */
public class SelectTemplate extends AbstractSqlTemplate<SelectQueryBuilder> {

    public SelectTemplate(SqlTemplates owner, Template<? super SelectQueryBuilder> parent) {
        super(owner, parent);
    }

    @Override
    protected String getShortString() {
        return "SQL-Select";
    }

    @Override
    protected Template<? super SelectQueryBuilder> createPartType(Object key) {
        switch (SqlQueryKey.key(key)) {
            case ATTRIBUTE:
                return newNodePart(SelectPart::new)
                        .andRequire(SqlQueryKey.TABLE);
            case TABLE:
                return constNodePart(new FromPart());
        }
        return null;
    }
    
    protected class FromPart implements StatementPart<SelectQueryBuilder> {
        @Override
        public void addTo(SelectQueryBuilder builder) {
            getOwner().getMainTable().appendTo(
                    builder.from());
        }
    }
    
    protected class SelectPart extends AbstractAttributeList implements StatementPart<SelectQueryBuilder> {

        public SelectPart(InternalComposer<?> query) {
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
