package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.map.MappedSelectRequest;
import org.cthul.miro.sql.map.MappedSqlType;

/**
 *
 */
public class PeopleImpl extends SqlEntitySet<Person, PeopleImpl> implements People {

    public PeopleImpl(MiConnection cnn, MappedSqlType<Person> type) {
        super(cnn, type);
    }

    public PeopleImpl(MiConnection cnn, MappedSelectRequest<Person> composer) {
        super(cnn, composer);
    }

    protected PeopleImpl(SqlEntitySet<Person, PeopleImpl> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        setUp(FETCH, "id", "firstName", "lastName");
    }

    @Override
    public People includeAddress() {
        return setUp(FETCH, "address.city");
    }

    @Override
    public People withFirstName(String name) {
        return setUp(PROPERTY_FILTER, "firstName", name);
    }

    @Override
    public People withLastName(String name) {
        return setUp(PROPERTY_FILTER, "lastName", name);
    }

    @Override
    public People withName(String first, String last) {
        return setUp(PROPERTY_FILTER, "firstName", first, "lastName", last);
    }
}
