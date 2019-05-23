package org.cthul.miro.db.string;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.Syntax;

public class SyntaxStringBuilder extends NestableStatementBuilder {
    
    private final Syntax syntax;
    private final MiDBString dBString;

    public SyntaxStringBuilder(Syntax syntax) {
        this(syntax, new MiDBStringBuilder());
    }

    public SyntaxStringBuilder(Syntax syntax, MiDBString dBString) {
        this.syntax = syntax;
        this.dBString = dBString;
    }

    protected MiDBString getDBString() {
        return dBString;
    }

    protected Syntax getSyntax() {
        return syntax;
    }

    @Override
    protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
        if (type == MiDBString.TYPE) {
            return type.cast(dBString);
        }
        return syntax.newClause(parent, type);
    }

    @Override
    public String toString() {
        return dBString.toString();
    }
    
    public void addTo(MiDBString target) {
        ((MiDBStringBuilder) dBString).addTo(target);
    }
}
