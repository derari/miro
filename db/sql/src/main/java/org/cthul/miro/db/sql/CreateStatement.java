package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.sql.CreateTableBuilder.Create;
import org.cthul.miro.db.stmt.MiUpdate;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public interface CreateStatement extends CreateTableBuilder.Create, MiUpdate {

    @Override
    Table table(QlCode name);

    @Override
    default Table table(String name) {
        return (Table) Create.super.table(name);
    }

    @Override
    default Table table(String... id) {
        return (Table) Create.super.table(id);
    }
    
    static CreateStatement create(MiConnection cnn) {
        return cnn.newStatement(SqlDDL.create());
    }
    
    interface Table extends CreateTableBuilder, MiUpdate {
        
    }
}
