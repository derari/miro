package org.cthul.miro.sql.migrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.sql.CreateStatement;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Cache;

/**
 *
 */
public class Table {
    
    private final MiConnection cnn;
    private final List<Column> columnOrder = new ArrayList<>();
    private final Map<String, Column> columns;
    private QlCode name;
    boolean create = false;

    public Table(MiConnection cnn, String name) {
        this.cnn = cnn;
        this.name = QlCode.id(name);
        columns = Cache.map(n -> {
            Column c = new Column(this, n);
            columnOrder.add(c);
            return c;
        });
    }

    public void setName(QlCode name) {
        this.name = name;
    }

    public QlCode getName() {
        return name;
    }
    
    public Column column(String name) {
        return columns.get(name);
    }
    
    void flush() throws MiException {
        if (create) {
            CreateStatement createStmt = CreateStatement.create(cnn);
            CreateStatement.Table t = createStmt.table(getName());
            columnOrder.forEach(c -> c.addTo(t));
            createStmt.execute();
        } else {
            throw new UnsupportedOperationException("ALTER TABLE");
        }
    }
    
}
