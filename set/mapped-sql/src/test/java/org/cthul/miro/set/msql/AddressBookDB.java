package org.cthul.miro.set.msql;

import org.cthul.miro.at.model.EntitySchemaBuilder;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.map.impl.QueryableEntitySet;

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
        this.people = new PeopleImpl(entitySet(Person.class, cnn), schemaBuilder.getSelectLayer(Person.class));
        schemaBuilder.getMappingBuilder(Address.class)
                .from("addresses a")
                .attribute("a.city");
        this.addressDao = new AddressDao(entitySet(Address.class, cnn), schemaBuilder.getSelectLayer(Address.class));
    }
    
    private <E> QueryableEntitySet<E> entitySet(Class<E> entityClass, MiConnection cnn) {
        QueryableEntitySet<E> es = new QueryableEntitySet<>(entityClass, graph);
        if (cnn != null) es.setConnection(cnn);
        return es;
    }

    public People people() {
        return people;
    }

    public AddressDao addresses() {
        return addressDao;
    }
}
