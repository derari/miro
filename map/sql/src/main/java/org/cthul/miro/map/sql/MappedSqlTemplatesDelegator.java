package org.cthul.miro.map.sql;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.util.Key;
import org.cthul.miro.view.composer.CRUDTemplateLayer;

/**
 *
 */
public class MappedSqlTemplatesDelegator<Entity> implements MappedSqlTemplatesBuilder<Entity> {
    
    private final MappedSqlTemplatesBuilder<Entity> delegatee;

    public MappedSqlTemplatesDelegator(MappedSqlTemplatesBuilder<Entity> delegatee) {
        this.delegatee = delegatee;
    }

    protected MappedSqlTemplatesBuilder<Entity> getDelegatee() {
        return delegatee;
    }

    @Override
    public MappedSqlTemplatesBuilder<Entity> mainTable(QlCode code, String key) {
        getDelegatee().mainTable(code, key);
        return this;
    }

    @Override
    public MappedSqlTemplatesBuilder<Entity> attribute(ResultScope scope, boolean key, String id, MiSqlParser.Attribute attribute) {
        getDelegatee().attribute(scope, key, id, attribute);
        return this;
    }

    @Override
    public <V extends Template<? super SqlFilterableClause>> MappedSqlTemplatesBuilder<Entity> where(Key<? super V> key, V filter) {
        getDelegatee().where(key, filter);
        return this;
    }

    @Override
    public <F> MappedSqlTemplatesBuilder<Entity> field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        getDelegatee().field(id, getter, setter);
        return this;
    }

    @Override
    public CRUDTemplateLayer<MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>> asLayer() {
        return getDelegatee().asLayer();
    }
}
