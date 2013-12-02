package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 */
public abstract class AbstractEntityConfiguration<Entity> extends EntityBuilderBase implements EntityConfiguration<Entity> {
    
    @Override
    public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
        return new Initializer(rs);
    }
    
    protected String[] getColumns() {
        return null;
    }

    protected String[][] getColumnGroups() {
        return null;
    }
    
    protected void setColumn(Entity entity, int colIndex, ResultSet rs, int index) throws SQLException {
        throw new IllegalArgumentException("Unexpected column #" + colIndex);
    }
    
    protected void setColumnGroup(Entity entity, int groupIndex, ResultSet rs, int[] indices) throws SQLException {
        throw new IllegalArgumentException("Unexpected column group #" + groupIndex);
    }
    
    protected class Initializer implements EntityInitializer<Entity> {
        
        private final ResultSet rs;
        private final int[] columns;
        private final int[][] columnGroups;

        public Initializer(ResultSet rs) throws SQLException {
            this.rs = rs;
            String[] colNames = getColumns();
            if (colNames == null) {
                columns = null;
            } else {
                columns = getFieldIndices(rs, getColumns());
            }
            final String[][] groups = getColumnGroups();
            if (groups == null) {
                columnGroups = null;
            } else {
                columnGroups = new int[groups.length][];
                for (int i = 0; i < groups.length; i++) {
                    columnGroups[i] = getFieldIndices(rs, groups[i]);
                }
            }
        }

        @Override
        public void apply(Entity entity) throws SQLException {
            if (columns != null) {
                for (int i = 0; i < columns.length; i++) {
                    setColumn(entity, i, rs, columns[i]);
                }
            }
            if (columnGroups != null) {
                for (int i = 0; i < columnGroups.length; i++) {
                    setColumnGroup(entity, i, rs, columnGroups[i]);
                }
            }
        }

        @Override
        public void complete() throws SQLException {
        }

        @Override
        public void close() throws SQLException {
        }
    }
}
