package org.cthul.miro.sql.set;

import org.cthul.miro.set.ValueSet;

/**
 *
 */
public interface People extends ValueSet<Person> {
    
    People withFirstName(String name);
}
