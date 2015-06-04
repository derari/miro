package org.cthul.miro.entity.base;

import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 *
 */
public abstract class BasicEntityType<Entity> implements EntityType<Entity> {

    private final String shortString;

    public BasicEntityType() {
        this.shortString = null;
    }

    public BasicEntityType(String shortString) {
        this.shortString = shortString;
    }
    
    @Override
    public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
        return new Factory(rs);
    }
    
    protected abstract Entity newEntity();

    @Override
    public String toString() {
        return getShortString();
    }

    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    protected class Factory implements EntityFactory<Entity> {
        
//        protected final MiResultSet resultSet;

        public Factory(MiResultSet resultSet) {
//            this.resultSet = resultSet;
        }

        @Override
        public Entity newEntity() throws MiException {
            return BasicEntityType.this.newEntity();
        }

        @Override
        public void complete() throws MiException {
        }

        @Override
        public void close() throws MiException {
        }

        @Override
        public String toString() {
            return "-> " + getShortString();
        }
    }
}
