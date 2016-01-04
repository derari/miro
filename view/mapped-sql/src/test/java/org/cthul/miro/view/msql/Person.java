package org.cthul.miro.view.msql;

import java.util.List;
import javax.persistence.*;
import org.cthul.miro.at.Where;
import org.cthul.miro.map.MappedQuery;

/**
 *
 */
@Table(name="PEOPLE")
public class Person {
    
    @Id
    @Column(name="ID")
    public int id;
    
    @Column(name="FIRST_NAME")
    public String firstName;

    @Column(name="LAST_NAME")
    public String lastName;
    
    @JoinColumn(name="ADDRESS_ID")
    @ManyToOne
    public Address address;
    
    @Transient
    public List<org.cthul.miro.test.Person> friends;
    
    public static interface PersonQuery extends MappedQuery<Person> {
        
        @Where("id =")
        PersonQuery withId(int id);
    }
}
