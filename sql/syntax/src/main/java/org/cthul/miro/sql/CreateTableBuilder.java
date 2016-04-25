package org.cthul.miro.sql;

import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public interface CreateTableBuilder {
    
    default Column column(String name) {
        return column(QlCode.id(name));
    }
    
    Column column(QlCode name);
    
    interface Create {
        
        default CreateTableBuilder table(String name) {
            return table(QlCode.id(name));
        }
        
        default CreateTableBuilder table(String... id) {
            return table(QlCode.id(id));
        }
        
        CreateTableBuilder table(QlCode name);
    }
    
    interface Column extends CreateTableBuilder {
        
        default Column type(String name) {
            return type(name, null);
        }
        
        Column type(String name, Integer scale);
        
        default Column nullable() {
            return nullable(true);
        }
        
        default Column notNullable() {
            return nullable(false);
        }
        
        Column nullable(boolean isNullable);
        
        default Column primaryKey() {
            return primaryKey(true);
        }
        
        Column primaryKey(boolean isKey);
        
        default Column autoGenerate() {
            return autoGenerate(true);
        }
        
        Column autoGenerate(boolean generate);
    }
}
