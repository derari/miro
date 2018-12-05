package org.cthul.miro.entity.map;

import org.cthul.miro.entity.map.ResultColumns.ColumnMatcher;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
import org.cthul.miro.entity.map.ResultColumns.ColumnsMatcher;

/**
 *
 * @param <Entity>
 * @param <Result>
 */
public abstract class SimpleMappedPropertyBuilder<Entity, Result> implements ColumnMappingBuilder<Entity, MappedPropertyBuilder.Single<Entity, Result>, MappedPropertyBuilder.Group<Entity, Result>> {
    
    private final Class<Entity> clazz;
    private String key;

    public SimpleMappedPropertyBuilder() {
        this.clazz = null;
    }

    public SimpleMappedPropertyBuilder(Class<Entity> clazz, String key) {
        this.clazz = clazz;
        this.key = key;
    }
    
    protected abstract Result build(MappedProperty<Entity> f);


    @Override
    public MappedPropertyBuilder.Single<Entity, Result> column(ColumnRule rule, String column) {
        if (key == null) key = column;
        return column(ResultColumns.match(rule, column)).setColumn(column);
    }

    @Override
    public MappedPropertyBuilder.Single<Entity, Result> column(ColumnMatcher column) {
        String k = key != null ? key : column.toString();
        return new MappedPropertyBuilder.Single<Entity, Result>(k, clazz, column) {
            @Override
            protected Result build(MappedProperty<Entity> f) {
                return SimpleMappedPropertyBuilder.this.build(f);
            }
        };
    }

    @Override
    public MappedPropertyBuilder.Group<Entity, Result> columns(ColumnRule allRule, ColumnRule eachRule, String... columns) {
        String k = key != null ? key : columns[0];
        return new MappedPropertyBuilder.Group<Entity, Result>(k, clazz, columns, allRule, eachRule) {
            @Override
            protected Result build(MappedProperty<Entity> f) {
                return SimpleMappedPropertyBuilder.this.build(f);
            }
        };
    }

    @Override
    public MappedPropertyBuilder.Group<Entity, Result> columns(ColumnsMatcher matcher, String[] columns) {
        String k = key != null ? key : columns[0];
        return new MappedPropertyBuilder.Group<Entity, Result>(k, clazz, columns, matcher) {
            @Override
            protected Result build(MappedProperty<Entity> f) {
                return SimpleMappedPropertyBuilder.this.build(f);
            }
        };
    }
}
