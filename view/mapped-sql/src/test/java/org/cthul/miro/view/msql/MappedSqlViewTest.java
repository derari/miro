package org.cthul.miro.view.msql;

import java.util.Arrays;
import org.cthul.miro.at.model.EntitySchemaBuilder;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.jdbc.JdbcConnection;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.view.ViewCRUD;
import org.cthul.miro.view.DslConnection;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MappedSqlViewTest {
    
    public DslConnection cnn = DslConnection.wrap(
            new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax()));
    
    public GraphSchema schema = new EntitySchemaBuilder();
    
    public ViewCRUD<?,Person.PersonQuery,?,?> person_view = 
            MappedSqlView.builder(schema, Person.class)
                .select(Person.PersonQuery.class)
                .build();
    
    @Before
    public void setUp() {
        TestDB.scenario1();
    }
    
    @After
    public void tearDown() throws MiException {
        cnn.close();
    }
    
    @Test
    public void test_by_id() throws MiException {
        Person p = cnn.select("firstName", "lastName")
                    .from(person_view).withId(1)
                    .execute().getSingle();
        assertThat(p.firstName, is("John"));
        assertThat(p.lastName, is("Doe"));
    }
    
    @Test
    public void test_graph() throws MiException {
        try (Graph graph = schema.newGraph(cnn);
                NodeSelector<Person> people = graph.nodeSelector(Person.class, FULL_NAME)) {
            Person p = people.get(1);
            people.complete();
            assertThat(p.firstName, is("John"));
            assertThat(p.lastName, is("Doe"));
        }
    }
    
    public static final Object FULL_NAME = Arrays.asList("firstName", "lastName");
    
}
