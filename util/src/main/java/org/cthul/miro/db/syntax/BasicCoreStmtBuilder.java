package org.cthul.miro.db.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author C5173086
 */
public class BasicCoreStmtBuilder implements CoreStmtBuilder {
    private final StringBuilder string = new StringBuilder();
    private final List<Object> arguments = new ArrayList<>();

    @Override
    public CoreStmtBuilder append(CharSequence chars) {
        string.append(chars);
        return this;
    }

    @Override
    public CoreStmtBuilder pushArgument(Object argument) {
        arguments.add(argument);
        return this;
    }

    @Override
    public CoreStmtBuilder pushArguments(Iterable<Object> args) {
        if (args instanceof Collection) {
            arguments.addAll((Collection<?>) args);
            return this;
        }
        return CoreStmtBuilder.super.pushArguments(args);
    }

    @Override
    public CoreStmtBuilder pushArguments(Object... args) {
        return pushArgument(Arrays.asList(args));
    }

    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return string.toString();
    }

    public void addTo(CoreStmtBuilder target) {
        target.append(string);
        target.pushArguments(arguments);
    }
    
}
