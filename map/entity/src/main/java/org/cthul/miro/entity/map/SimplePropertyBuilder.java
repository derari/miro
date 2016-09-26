package org.cthul.miro.entity.map;

import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultColumns.ColumnMatcher;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 * @param <Result>
 */
public abstract class SimplePropertyBuilder<Entity, Cnn, Result> implements EntityPropertyBuilder<Entity, Cnn, Result> {
    
    private final Class<Entity> clazz;
    private String key;

    public SimplePropertyBuilder() {
        this.clazz = null;
    }

    public SimplePropertyBuilder(Class<Entity> clazz, String key) {
        this.clazz = clazz;
        this.key = key;
    }
    
    protected abstract Result build(EntityAttribute<Entity, Cnn> f);

    @Override
    public EntityPropertyBuilder<Entity, Cnn, Result> as(String key) {
        this.key = key;
        return this;
    }

    @Override
    public Single<Entity, Cnn, Result> column(ColumnRule rule, String column) {
        if (key == null) key = column;
        return column(ResultColumns.match(rule, column)).addColumns(column);
    }

    @Override
    public Single<Entity, Cnn, Result> column(ColumnMatcher column) {
        String k = key != null ? key : column.toString();
        return new Single<Entity, Cnn, Result>(k, clazz, column) {
            @Override
            protected Result build(EntityAttribute<Entity, Cnn> f) {
                return SimplePropertyBuilder.this.build(f);
            }
        };
    }

    @Override
    public Group<Entity, Cnn, Result> columns(ColumnRule allRule, ColumnRule eachRule, String... columns) {
        String k = key != null ? key : columns[0];
        return new Group<Entity, Cnn, Result>(k, clazz, columns, allRule, eachRule) {
            @Override
            protected Result build(EntityAttribute<Entity, Cnn> f) {
                return SimplePropertyBuilder.this.build(f);
            }
        };
    }
}
