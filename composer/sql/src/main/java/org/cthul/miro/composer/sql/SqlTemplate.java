package org.cthul.miro.composer.sql;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.ComposerKey;
import static org.cthul.miro.composer.ComposerParts.*;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.sql.SqlQueryKey.AttributeList;
import org.cthul.miro.db.sql.SqlClause;
import org.cthul.miro.db.sql.SqlFilterableClause;

/**
 *
 * @param <Builder>
 */
public class SqlTemplate<Builder extends SqlFilterableClause> extends AbstractSqlTemplate<Builder> {

    public SqlTemplate(SqlTemplates owner, Template<? super Builder> parent) {
        super(owner, parent);
    }

    @Override
    protected String getShortString() {
        return "SQL-misc";
    }
    
    @Override
    protected Template<? super Builder> createPartType(Object key) {
        if (key instanceof SqlTemplateKey) {
            return ((SqlTemplateKey) key).asTemplate(getOwner());
        }
        switch (ComposerKey.key(key)) {
            case PHASE:
                return parentPartType(ComposerKey.PHASE)
                        .andLink(SqlQueryKey.ATTRIBUTE);
            case RESULT:
                return parentPartType(ComposerKey.RESULT)
                        .andNewNode(ic -> {
                            AttributeList attributes = ic.node(SqlQueryKey.ATTRIBUTE);
                            return attributeKey -> {
                                Attribute at = getOwner().getAttributes().get(attributeKey);
                                attributes.add(at);
                            };
                        });
            case FETCH_KEYS:
                return parentPartType(ComposerKey.FETCH_KEYS)
                        .andDo(ic -> {
                            AttributeList attributes = ic.node(SqlQueryKey.ATTRIBUTE);
                            attributes.addAll(getOwner().getKeyAttributes());
                        });
                
        }
        switch (SqlQueryKey.key(key)) {
            case DEFAULT_ATTRIBUTES:
                return setUp(SqlQueryKey.ATTRIBUTE, atList -> {
                    getOwner().getDefaultAttributes().forEach(atList::add);
                });
            case FIND_BY_KEYS:
                return newNodePart(FindByKeys::new);
        }
        
        Template<?> t = getOwner().getTemplates().get(key);
        if (t != null) return (Template) t;
        return null;
    }

    protected class FindByKeys implements StatementPart<SqlFilterableClause>, SqlQueryKey.FindByKeys {

        private final List<Object[]> keys = new ArrayList<>();
        private final int len = getOwner().getKeyAttributes().size();
        
        @Override
        public void addTo(SqlFilterableClause builder) {
            SqlClause.Where<?> where = builder.where();
            if (len == 1) {
                where
                    .ql(getOwner().getKeyAttributes().get(0).expression())
                    .in().list(keys.stream().map(k -> k[0]));
            } else {
                SqlClause.Junction<?> j = where.either();
                keys.forEach(k -> {
                    SqlClause.Conjunction<?> c = j.or().all();
                    for (int i = 0; i < len; i++) {
                        c.ql(getOwner().getKeyAttributes().get(i).expression())
                         .ql(" = ?")
                         .pushArgument(k[i]);
                    }
                });
            }
        }

        @Override
        public void addAll(List<Object[]> keys) {
            this.keys.addAll(keys);
        }
    }
}
