package org.cthul.miro.migrate.sql;

import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public class Schema {
    
    private final SqlDB db;
    private final boolean isDefaultSchema;
    private QlCode name;

    public Schema(SqlDB db, String name) {
        this.db = db;
        this.name = QlCode.id(name);
        this.isDefaultSchema = name.isEmpty();
    }

    public void setName(QlCode name) {
        this.name = name;
    }

    public QlCode getName() {
        return name;
    }
    
    protected QlCode tableName(QlCode code) {
        if (isDefaultSchema) {
            return code;
        } else {
            return QlCode.build()
                    .append(getName()).append(".").append(code);
        }
    }
    
    public Table alterTable(QlCode name) {
        return db.table(tableName(name));
    }
    
    public Table createTable(QlCode name) {
        Table t = alterTable(name);
        t.create = true;
        return t;
    }
    
    public Table alterTable(String name) {
        QlCode n = tableName(QlCode.id(name));
        return db.table(SqlDB.toString(n));
    }
    
    public Table createTable(String name) {
        Table t = alterTable(name);
        t.create = true;
        return t;
    }
}
