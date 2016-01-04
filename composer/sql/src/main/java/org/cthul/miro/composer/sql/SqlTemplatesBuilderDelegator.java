package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Key;

/**
 *
 * @param <This>
 */
public class SqlTemplatesBuilderDelegator<This extends SqlTemplatesBuilder<This>>
                implements SqlTemplatesBuilder<This> {
    
    private final SqlTemplatesBuilder<?> delegatee;

    public SqlTemplatesBuilderDelegator(SqlTemplatesBuilder<?> delegatee) {
        this.delegatee = delegatee;
    }

    protected SqlTemplatesBuilder<?> getSqlDelegatee() {
        return delegatee;
    }
    
    protected This self() {
        return (This) this;
    }
    @Override
    public This mainTable(QlCode code, String key) {
        getSqlDelegatee().mainTable(code, key);
        return self();
    }

    @Override
    public This attribute(ResultScope scope, boolean key, String id, MiSqlParser.Attribute attribute) {
        getSqlDelegatee().attribute(scope, key, id, attribute);
        return self();
    }

    @Override
    public <V extends Template<? super SqlFilterableClause>> This where(Key<? super V> key, V filter) {
        getSqlDelegatee().where(key, filter);
        return self();
    }
}
