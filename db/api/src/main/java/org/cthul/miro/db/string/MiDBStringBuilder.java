package org.cthul.miro.db.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Basic implementation for a {@link MiDBString}.
 */
public class MiDBStringBuilder implements MiDBString {
    
    private final StringBuilder string;
    private final List<Object> arguments;

    public MiDBStringBuilder() {
        this.string = new StringBuilder();
        this.arguments = new ArrayList<>();
    }

    protected MiDBStringBuilder(MiDBStringBuilder source) {
        this.string = source.string;
        this.arguments = source.arguments;
    }

    
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
