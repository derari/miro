package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.set.AbstractQuerySet;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.sql.map.MappedSelectRequest;
import org.cthul.miro.sql.map.MappedSqlType;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public class MappedDeleteSet<Entity, This extends MappedDeleteSet<Entity,This>> extends AbstractQuerySet<Entity, MappedSelectRequest<Entity>, This> {

    public MappedDeleteSet(MiConnection cnn, MappedSqlType<Entity> type) {
        super(cnn, request(SqlDQML.select(), type::newMappedQueryRequest));
    }

    protected MappedDeleteSet(MappedDeleteSet<Entity, This> source) {
        super(source);
    }
}
