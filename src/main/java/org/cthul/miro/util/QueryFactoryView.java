package org.cthul.miro.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;

/**
 * Uses reflection to create queries. 
 * Expects a {@code (MiConnection, String[])} constructor.
 * @param <Qry> 
 */
public class QueryFactoryView<Qry> implements View<Qry> {

    private final Constructor<Qry> newQuery;
    private final boolean hasViewArg;

    public QueryFactoryView(Class<Qry> clazz) {
        Constructor<Qry> c;
        boolean viewArg = false;
        try {
            c = clazz.getConstructor(MiConnection.class, String[].class);
        } catch (NoSuchMethodException e) {
            try {
                c = clazz.getConstructor(MiConnection.class, String[].class, View.class);
                viewArg = true;
            } catch (NoSuchMethodException e2) {
                // throw first exception
                throw new RuntimeException(e);
            }
        }
        newQuery = c;
        hasViewArg = viewArg;
    }

    @Override
    public Qry select(MiConnection cnn, String[] select) {
        try {
            if (hasViewArg) {
                return newQuery.newInstance(cnn, select, this);
            } else {
                return newQuery.newInstance(cnn, select);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
