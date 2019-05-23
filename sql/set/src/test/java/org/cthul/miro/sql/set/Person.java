package org.cthul.miro.sql.set;

import java.util.List;
import javax.persistence.*;
import org.cthul.miro.at.model.Alias;

/**
 *
 */
@Table(name="PEOPLE") @Alias("ppl")
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
    public transient Address address;
    
    public transient List<Person> friends;

    @Override
    public String toString() {
        return id + "/" + firstName + " " + lastName;
    }
}
