package org.cthul.miro.sql.set;

import org.cthul.miro.sql.set.EntitySchemaBuilder;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.graph.Graph;

/**
 *
 */
public class AddressBookDB {
    
    private final Graph graph;
    private final People people;
    private final AddressDao addressDao;

    public AddressBookDB(MiConnection cnn) {
        this(new EntitySchemaBuilder(), cnn);
    }
    
    private AddressBookDB(EntitySchemaBuilder schemaBuilder, MiConnection cnn) {
        this(schemaBuilder, schemaBuilder.newFakeGraph(cnn), cnn);
    }

    private AddressBookDB(EntitySchemaBuilder schemaBuilder, Graph graph, MiConnection cnn) {
        this.graph = graph;
        this.people = new PeopleImpl(cnn, schemaBuilder.getSelectLayer(Person.class));
        schemaBuilder.getMappingBuilder(Address.class)
                .from("Addresses a")
                .attributes("a.city, a.street");
        this.addressDao = new AddressDao(cnn, schemaBuilder.getSelectLayer(Address.class));
    }

    public People people() {
        return people;
    }

    public AddressDao addresses() {
        return addressDao;
    }
}
