package org.cthul.miro.sql.set;

import java.util.List;

/**
 *
 */
public class Address {

    public int id;
    public String street;
    public String city;
    public transient List<Person> people;
}
