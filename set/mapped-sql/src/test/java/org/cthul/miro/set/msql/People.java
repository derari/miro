package org.cthul.miro.set.msql;

import org.cthul.miro.set.ValueSet;

/**
 *
 */
public interface People extends ValueSet<Person> {
    
    People withFirstName(String name);
}
