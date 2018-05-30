package org.cthul.miro.sql.set;

import org.cthul.miro.set.ValueSet;

/**
 *
 */
public interface People extends ValueSet<Person> {
    
    People includeAddress();
    
    People withFirstName(String name);
    
    People withLastName(String name);
    
    People withName(String first, String last);
}
