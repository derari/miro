package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.CreateTableBuilder.Create;
import org.cthul.miro.db.request.MiUpdate;
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
        return cnn.newRequest(SqlDDL.create());
    }
    
    interface Table extends CreateTableBuilder, MiUpdate {
        
    }
}
