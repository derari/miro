package org.cthul.miro.sql.set;

import java.util.List;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.result.Results.Action;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class MappedSelectComposerTest {
    
    MappedSelectComposer cmp;
    
    @Before
    public void setUp() {
        TestDB.scenario1();
        MappedSqlType<Person> type = new MappedSqlType<>(Person.class)
                .sql("SELECT p.id, p.first_name AS firstName, p.last_name AS lastName FROM People p")
                .column("id").field("id")
                .column("firstName").field("firstName")
                .column("lastLame").field("lastName");
        
        cmp = type.newMappedSelectComposer();
    }
    
    @Test
    public void test() {
        cmp.getFetchedProperties().addAll("id", "firstName");
        Action<Person> qry = getQueryString();
        List<Person> people = qry._asList();
        assertThat(TestDB.lastStmt, is("SELECT p.id, p.first_name AS firstName FROM People p"));
        assertThat(people.size(), is(3));
        assertThat(people.get(0).firstName, is("John"));
    }
    
    private Action<Person> getQueryString() {
        MappedQuery<Person,SelectQuery> qry = new MappedQuery<>(TestDB.getMiConnection(), SqlDQML.select());
        return qry.query(ComposerState.asRequestComposer(cmp));
    }
}
