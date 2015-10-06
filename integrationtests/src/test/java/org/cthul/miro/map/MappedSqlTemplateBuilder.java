package org.cthul.miro.map;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.sql.SqlTemplateBuilder;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.base.BasicEntityType;

/**
 *
 * @param <Entity>
 */
public class MappedSqlTemplateBuilder<Entity> {
    
    private final EntityType<? extends Entity> entityType;
    private final MappedTemplateBuilder<Entity> mapping;
    private final SqlTemplateBuilder sql;

    public MappedSqlTemplateBuilder(Class<? extends Entity> clazz) {
        this(BasicEntityType.build(clazz));
    }
    
    public MappedSqlTemplateBuilder(EntityType<? extends Entity> entityType) {
        this.entityType = entityType;
        this.mapping = new MappedTemplateBuilder<>();
        this.sql = new SqlTemplateBuilder();
    }
    
    public <B extends StatementHolder<? extends SelectQueryBuilder> & EntitySetup<? extends Entity>> Template<? super B> getSelectTemplate() {
        Template<? super B> sqlTemplate = sql.getSelectTemplate()
                .adapt(StatementHolder<? extends SelectQueryBuilder>::getStatement);
        return mapping.buildSelectTemplate(sqlTemplate);
    }
    
    public void from(String from) {
        sql.from(from);
    }
    
    public <T> void attribute(String key, Function<? super Entity, ? extends T> getter, BiConsumer<? super Entity, ? super T> setter, String column) {
        sql.attribute(key, column);
    }
}
