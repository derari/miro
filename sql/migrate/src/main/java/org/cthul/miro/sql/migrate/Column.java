package org.cthul.miro.sql.migrate;

import org.cthul.miro.sql.CreateStatement;
import org.cthul.miro.sql.CreateTableBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public class Column implements CreateTableBuilder.Column {
    
    private final Table owner;
    private QlCode name;
    private String type = "INTEGER";
    private Integer scale = null;
    private boolean nullable = true;
    private boolean primaryKey = false;
    private boolean autoGenerate = false;

    public Column(Table owner, String name) {
        this.owner = owner;
        this.name = QlCode.id(name);
    }

    public void setName(QlCode name) {
        this.name = name;
    }

    public QlCode getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Integer getScale() {
        return scale;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }
    
    @Override
    public Column type(String name, Integer scale) {
        this.type = name;
        this.scale = scale;
        return this;
    }

    @Override
    public Column nullable(boolean isNullable) {
        this.nullable = isNullable;
        return this;
    }

    @Override
    public Column primaryKey(boolean isKey) {
        this.primaryKey = isKey;
        return this;
    }

    @Override
    public Column autoGenerate(boolean generate) {
        this.autoGenerate = generate;
        return this;
    }

    @Override
    public Column column(String name) {
        return owner.column(name);
    }

    protected void addTo(CreateStatement.Table t) {
        t.column(getName())
                .type(getType(), getScale())
                .nullable(isNullable())
                .autoGenerate(isAutoGenerate())
                .primaryKey(isPrimaryKey());
    }

    @Override
    public Column column(QlCode name) {
        throw new UnsupportedOperationException();
    }
}
