package org.cthul.miro.db.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * Basic implementation for a {@link MiDBString}.
 */
public class MiDBStringBuilder extends AbstractComposableBuilder implements MiDBString {
    
    private final StringBuilder string;
    private final List<Object> arguments;
    private MiDBStringBuilder nested;

    public MiDBStringBuilder() {
        this.string = new StringBuilder();
        this.arguments = new ArrayList<>();
    }

    protected MiDBStringBuilder(MiDBStringBuilder source) {
        this.string = source.string;
        this.arguments = source.arguments;
    }

    @Override
    protected MiDBString getBuilderForNestedClause() {
        if (nested == null) {
            nested = new MiDBStringBuilder(this);
        }
        return nested;
    }

    public QlBuilder<?> asQlBuilder(Syntax syntax) {
        return newNestedClause(str -> QlBuilder.create(syntax, str));
    }
    
    public <Clause> Clause as(Syntax syntax, ClauseType<Clause> type) {
        return newNestedClause(str -> syntax.newClause(str, type));
    }
    
    public <Clause> Clause as(Function<MiDBString, Clause> function) {
        return newNestedClause(function);
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
        close();
        target.append(string);
        target.pushArguments(arguments);
    }
}
