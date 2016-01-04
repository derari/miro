package org.cthul.miro.view.msql;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.cthul.miro.at.model.EntitySchemaBuilder;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.jdbc.JdbcConnection;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.view.DslConnection;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MappedQueryStringTest {
    
    public DslConnection cnn = DslConnection.wrap(
            new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax()));
    
    public GraphSchema schema = new EntitySchemaBuilder();
    
    public MappedQueryString<Person> PERSON_BY_ID = MappedQueryString
            .forType(schema, Person.class)
            .query("SELECT first_name AS `firstName`, last_name AS `lastName` "
                 + "FROM People WHERE id = ?")
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
    public void test_query() throws MiException, InterruptedException, ExecutionException {
        Person p = cnn.select()
                .from(PERSON_BY_ID)
                .with(1).getSingle();
        assertThat(p.firstName, is("John"));
        assertThat(p.lastName, is("Doe"));
    }
    
    public static final Object FULL_NAME = Arrays.asList("firstName", "lastName");
    
}
