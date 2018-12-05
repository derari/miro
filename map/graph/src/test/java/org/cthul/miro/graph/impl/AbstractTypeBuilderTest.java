package org.cthul.miro.graph.impl;

import java.util.Collection;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.domain.Domain;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.domain.impl.AbstractTypeBuilder;
import org.cthul.miro.domain.impl.DomainBuilder;
import org.cthul.miro.entity.EntityFactory;
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
        DomainBuilder schema = Domain.build();
        schema.put(Integer.class, new AbstractTypeBuilderImpl());
        MiResultSet rs = new IntResultSet();
        EntityFactory<Integer> ef = schema.newUncachedRepository(null).getEntitySet(Integer.class).getLookUp().newFactory(rs);
        assertThat(ef.newEntity(), is(1));
    }

    public class AbstractTypeBuilderImpl extends AbstractTypeBuilder<Integer, AbstractTypeBuilderImpl> {

        public AbstractTypeBuilderImpl() {
            super(Integer.class);
            keys("i");
            property("i").requiredColumn("i").readOnly();
            constructor(args -> (Integer) args[0]);
        }

        @Override
        protected BatchLoader<Integer> newBatchLoader(Repository repository, MiConnection connection, Collection<?> properties) {
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
