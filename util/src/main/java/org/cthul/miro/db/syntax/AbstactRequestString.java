package org.cthul.miro.db.syntax;

import org.cthul.miro.db.syntax.RequestString;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstactRequestString implements RequestString {
    
    private final StringBuilder queryString = new StringBuilder();
    private final List<Object> arguments = new ArrayList<>();

    public AbstactRequestString() {
    }

    @Override
    public RequestString append(String query) {
        queryString.append(query);
        return this;
    }

    @Override
    public abstract RequestString identifier(String id);

    @Override
    public abstract RequestString stringLiteral(String string);

    @Override
    public RequestString clearArguments() {
        arguments.clear();
        return this;
    }

    @Override
    public RequestString pushArgument(Object arg) {
        arguments.add(arg);
        return this;
    }

    @Override
    public RequestString setArgument(int index, Object arg) {
        while (arguments.size() <= index) arguments.add(null);
        arguments.set(index, arg);
        return this;
    }

    @Override
    public String toString() {
        return queryString.toString();
    }

//    @Override
    public int length() {
        return queryString.length();
    }
    
    @Override
    public List<Object> getArguments() {
        return arguments;
    }
}
