package org.cthul.miro.sql.set2;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.map.MappedSqlDomain;
import org.cthul.miro.sql.set.Person;

public class AddressBookDB {
    
    private final PeopleDao peopleDao;

    public AddressBookDB(MiConnection cnn) {
        this(new MappedSqlDomain(), cnn);
    }
    
    private AddressBookDB(MappedSqlDomain schemaBuilder, MiConnection cnn) {
        this.peopleDao = new PeopleDao(cnn, schemaBuilder.getMappedSqlType(Person.class));
    }
    
    public PeopleDao.FiltersOrValues people() {
        return peopleDao.getDao();
    }
}
