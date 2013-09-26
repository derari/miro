package org.cthul.miro.dsl;

/**
 *
 */
public abstract class ViewBase<Qry> implements View<Qry> {

    @Override
    public Qry select(String... fields) {
        return select(null, fields);
    }
}
