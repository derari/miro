package org.cthul.miro.entity.map;

import org.cthul.miro.entity.base.ResultColumns.ColumnRule;

/**
 *
 * @param <Entity>
 * @param <Result>
 */
public abstract class SimpleAttributeBuilder<Entity, Result> implements EntityAttributeBuilder<Entity, Result> {
    
    private final Class<Entity> clazz;
    private String key;

    public SimpleAttributeBuilder() {
        this.clazz = null;
    }

    public SimpleAttributeBuilder(Class<Entity> clazz, String key) {
        this.clazz = clazz;
        this.key = key;
    }
    
    protected abstract Result build(EntityAttribute<Entity> f);

    @Override
    public EntityAttributeBuilder<Entity, Result> as(String key) {
        this.key = key;
        return this;
    }

    @Override
    public Single<Entity, Result> column(ColumnRule rule, String column) {
        String k = key != null ? key : column;
        return new Single<Entity, Result>(k, clazz, column, rule) {
            @Override
            protected Result build(EntityAttribute<Entity> f) {
                return SimpleAttributeBuilder.this.build(f);
            }
        };
    }

    @Override
    public Group<Entity, Result> columns(ColumnRule allRule, ColumnRule eachRule, String... columns) {
        String k = key != null ? key : columns[0];
        return new Group<Entity, Result>(k, clazz, columns, allRule, eachRule) {
            @Override
            protected Result build(EntityAttribute<Entity> f) {
                return SimpleAttributeBuilder.this.build(f);
            }
        };
    }
}
