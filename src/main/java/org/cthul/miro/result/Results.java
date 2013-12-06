package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.cursor.ResultCursor;

public class Results<Entity> implements AutoCloseable {
    
    private static final ResultBuilder RESULT_BUILDER = new ResultBuilder() {
        @Override
        public Object build(ResultSet rs, EntityType type, EntityConfiguration config) throws SQLException {
            return new Results(rs, type, config);
        }
    };
    
    public static <E> ResultBuilder<Results<E>, E> getBuilder() {
        return RESULT_BUILDER;
    }
    
    private final ResultSet rs;
    private final EntityType<Entity> type;
    private final EntityConfiguration<? super Entity> config;

    public Results(ResultSet rs, EntityType<Entity> type, EntityConfiguration<? super Entity> config) {
        this.rs = rs;
        this.type = type;
        this.config = config;
    }
    
    public List<Entity> asList() throws SQLException {
        return ResultBuilders.<Entity>getListResult().build(rs, type, config);
    }
    
    public Entity[] asArray() throws SQLException {
        return ResultBuilders.<Entity>getArrayResult().build(rs, type, config);
    }
    
    public ResultCursor<Entity> asCursor() throws SQLException {
        return ResultBuilders.<Entity>getCursorResult().build(rs, type, config);
    }
    
    public Entity getFirst() throws SQLException {
        return ResultBuilders.<Entity>getFirstResult().build(rs, type, config);
    }
    
    public Entity getSingle() throws SQLException {
        return ResultBuilders.<Entity>getSingleResult().build(rs, type, config);
    }

    @Override
    public void close() throws SQLException {
        rs.close();
    }
    
    public void noResult() throws SQLException {
        asList();
    }
}
