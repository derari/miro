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
public class MappedSelectSet<Entity, This extends MappedSelectSet<Entity,This>> extends AbstractQuerySet<Entity, MappedSelectRequest<Entity>, This> {

    public MappedSelectSet(MiConnection cnn, MappedSqlType<Entity> type) {
        super(cnn, request(SqlDQML.select(), type::newMappedQueryRequest));
    }

    protected MappedSelectSet(MappedSelectSet<Entity, This> source) {
        super(source);
    }
}
