package org.cthul.miro.graph.impl;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AbstractTypeBuilderTest {
    
    public AbstractTypeBuilderTest() {
    }

    @Test
    public void factoryWithConstructor() throws MiException {
        GraphSchemaBuilder schema = GraphSchema.build();
        schema.put(Integer.class, new AbstractTypeBuilderImpl());
        MiResultSet rs = new IntResultSet();
        EntityFactory<Integer> ef = schema.getEntityType(Integer.class).newFactory(rs);
        assertThat(ef.newEntity(), is(1));
    }

    public class AbstractTypeBuilderImpl extends AbstractTypeBuilder<Integer, AbstractTypeBuilderImpl> {

        public AbstractTypeBuilderImpl() {
            super(Integer.class);
            keys("i");
            require("i").readOnly();
            constructor(args -> (Integer) args[0]);
        }

        @Override
        protected BatchLoader<Integer> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            throw new UnsupportedOperationException();
        }
    }
    
    public class IntResultSet implements MiResultSet {

        boolean end = false;
        
        @Override
        public boolean next() throws MiException {
            if (end) return false;
            return end = true;
        }

        @Override
        public boolean previous() throws MiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(int columnIndex) throws MiException {
            return 1;
        }

        @Override
        public int getInt(int columnIndex) throws MiException {
            return 1;
        }

        @Override
        public long getLong(int columnIndex) throws MiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getString(int columnIndex) throws MiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAfterLast() throws MiException {
            return end;
        }

        @Override
        public void close() throws MiException {
        }

        @Override
        public int getColumnCount() throws MiException {
            return 1;
        }

        @Override
        public String getColumnLabel(int columnIndex) throws MiException {
            return "i";
        }
    }
}
