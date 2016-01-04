package org.cthul.miro.view.impl;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.view.ViewR;
import org.cthul.miro.view.DslConnection;

/**
 *
 */
public class DslConnectionDelegator implements DslConnection {
    
    private final MiConnection connection;

    public DslConnectionDelegator(MiConnection connection) {
        this.connection = connection;
    }

    @Override
    public MiQueryString newQuery() {
        return connection.newQuery();
    }

    @Override
    public MiUpdateString newUpdate() {
        return connection.newUpdate();
    }

    @Override
    public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
        return connection.newStatement(type);
    }

    @Override
    public Object insert(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Select select(List<?> attributes) {
        return new DslSelect(attributes);
    }

    @Override
    public Object update(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object delete(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void close() throws MiException {
        connection.close();
    }
    
    protected class DslStep {
        
        private final List<?> attributes;

        public DslStep(List<?> attributes) {
            this.attributes = attributes;
        }

        public List<?> getAttributes() {
            List<Object> list = new ArrayList<>();
            list.add(DslConnectionDelegator.this);
            list.addAll(attributes);
            return list;
        }
    }
    
    protected class DslSelect extends DslStep implements Select {

        public DslSelect(List<?> attributes) {
            super(attributes);
        }

        @Override
        public <V> V from(ViewR<V> view) {
            return view.select(getAttributes());
        }
    }
}
