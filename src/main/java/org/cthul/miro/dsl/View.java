package org.cthul.miro.dsl;

import org.cthul.miro.MiConnection;

/**
 * Represents a view to the database that can be queried.
 * @param <Qry> 
 */
public interface View<Qry> {

    Qry select(MiConnection cnn, String[] fields);
}
