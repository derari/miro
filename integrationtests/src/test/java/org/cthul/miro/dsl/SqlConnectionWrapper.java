package org.cthul.miro.dsl;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.syntax.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.view.ViewR;

/**
 *
 */
public class SqlConnectionWrapper implements SqlConnection, MiConnection {
    
    private final MiConnection connection;

    public SqlConnectionWrapper(MiConnection connection) {
        this.connection = connection;
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public Select select(List<?> attributes) {
        return new Select() {
            @Override
            public <Query> Query from(ViewR<Query> view) {
                List<Object> att = new ArrayList<>(attributes.size()+1);
                att.add(this);
                att.addAll(attributes);
                return view.select(att);
            }
        };
    }

    public MiConnection getConnection() {
        return connection;
    }

    @Override
    public MiQueryString newQuery() {
        return getConnection().newQuery();
    }

    @Override
    public MiUpdateString newUpdate() {
        return getConnection().newUpdate();
    }

    @Override
    public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
        return getConnection().newStatement(type);
    }
}
