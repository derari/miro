package org.cthul.miro.db.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.cthul.miro.db.stmt.MiDBString;

/**
 * Basic implementation for a {@link MiDBString}.
 */
public class BasicDBStringBuilder implements MiDBString {
    
    private final StringBuilder string = new StringBuilder();
    private final List<Object> arguments = new ArrayList<>();

    @Override
    public MiDBString append(CharSequence chars) {
        string.append(chars);
        return this;
    }

    @Override
    public MiDBString pushArgument(Object argument) {
        arguments.add(argument);
        return this;
    }

    @Override
    public MiDBString pushArguments(Iterable<?> args) {
        if (args instanceof Collection) {
            arguments.addAll((Collection<?>) args);
            return this;
        }
        return MiDBString.super.pushArguments(args);
    }

    @Override
    public MiDBString pushArguments(Object... args) {
        return pushArgument(Arrays.asList(args));
    }

    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return string.toString();
    }

    public void addTo(MiDBString target) {
        target.append(string);
        target.pushArguments(arguments);
    }
    
    
}
