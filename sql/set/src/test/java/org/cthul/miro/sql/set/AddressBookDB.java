package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.sql.map.MappedSqlDomain;

/**
 *
 */
public class AddressBookDB {
//    
//    private final Repository graph;
//    private final People people;
//    private final AddressDao addressDao;
//
//    public AddressBookDB(MiConnection cnn) {
//        this(new MappedSqlDomain(), cnn);
//    }
//    
//    private AddressBookDB(MappedSqlDomain schemaBuilder, MiConnection cnn) {
//        this(schemaBuilder, schemaBuilder.newUncachedRepository(cnn), cnn);
//    }
//
//    private AddressBookDB(MappedSqlDomain schemaBuilder, Repository graph, MiConnection cnn) {
//        this.graph = graph;
//        this.people = new PeopleImpl(cnn, schemaBuilder.newMappedSelectRequest(Person.class));
//        schemaBuilder.getMappingBuilder(Address.class)
//                .from("Addresses a")
//                .attributes("a.city, a.street");
//        this.addressDao = new AddressDao(cnn, schemaBuilder.newMappedSelectRequest(Address.class));
//    }
//
//    public People people() {
//        return people;
//    }
//
//    public AddressDao addresses() {
//        return addressDao;
//    }
}
