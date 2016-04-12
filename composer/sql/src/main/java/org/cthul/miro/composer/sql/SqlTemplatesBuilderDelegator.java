package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.util.Key;

/**
 *
 * @param <This>
 */
public interface SqlTemplatesBuilderDelegator<This extends SqlTemplatesBuilder<This>>
                extends SqlTemplatesBuilder<This> {
    
    SqlTemplatesBuilder<?> internalSqlTemplatesBuilder();

    @Override
    default This attribute(SqlAttribute attribute) {
        internalSqlTemplatesBuilder().attribute(attribute);
        return (This) this;
    }

    @Override
    default This table(SqlTable table) {
        internalSqlTemplatesBuilder().table(table);
        return (This) this;
    }

    @Override
    default <V extends Template<? super SqlFilterableClause>> This where(Key<? super V> key, V filter) {
        internalSqlTemplatesBuilder().where(key, filter);
        return (This) this;
    }
}
