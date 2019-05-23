package org.cthul.miro.sql.set2;

import java.util.List;
import org.cthul.miro.sql.set.Person;
import org.cthul.miro.sql.set.TestDB;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class MappedSetTest {
    
    private AddressBookDB db = new AddressBookDB(TestDB.getMiConnection());
   
    @Before
    public void setUp() {
        TestDB.scenario1();
    }
    
    @Test
    public void testGetPeopleWithFirstName() {
        List<Person> people = db.people().withFirstName("Bob").read().getList();
        assertThat(people.size(), is(1));
        assertThat(people.get(0).toString(), is("3/Bob Brown"));
    }
    
    @Test
    public void testDeletePeopleWithFirstName() {
        db.people().withFirstName("Bob").delete();
    }
    
//    @Test
//    public void testFillValues() {
//        Person bob = new Person();
//        bob.id = 3;
//        db.people().values(bob).read().getList();
//        assertThat(bob.toString(), is("3/Bob Brown"));        
//    }
    
}
