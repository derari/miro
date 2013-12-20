package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;

public interface StringQueryBuilder<Builder extends StringQueryBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder query(String query);
    
    Builder batch(Object... values);
}
