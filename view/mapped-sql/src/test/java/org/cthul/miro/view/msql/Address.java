package org.cthul.miro.view.msql;

import java.util.List;
import javax.persistence.*;

/**
 *
 */
@Table(name="ADDRESSES")
public class Address {

    @Id
    @Column(name="ID")
    public int id;
    
    @Column(name="STREET")
    public String street;
    
    @Column(name="CITY")
    public String city;
    
    @Transient
    public List<Person> people;
}
