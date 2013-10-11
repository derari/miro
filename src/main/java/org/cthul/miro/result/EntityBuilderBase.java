package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class EntityBuilderBase {
    
    protected int[] getFieldIndices(ResultSet rs, String... fields) throws SQLException {
        final int[] indices = new int[fields.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = rs.findColumn(fields[i]);
        }
        return indices;
    }

    protected int[] getFieldIndices(ResultSet rs, List<String> fields) throws SQLException {
        final int[] indices = new int[fields.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = rs.findColumn(fields.get(i));
        }
        return indices;
    }
    
    protected int getFieldIndex(ResultSet rs, String field) throws SQLException {
        return rs.findColumn(field);
    }
}
