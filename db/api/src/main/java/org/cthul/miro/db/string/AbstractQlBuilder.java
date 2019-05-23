package org.cthul.miro.db.string;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * Foundation for implementing a {@link QlBuilder} 
 * on top of a {@link MiDBString}.
 * @param <This>
 */
public abstract class AbstractQlBuilder<This extends QlBuilder<This>> extends SyntaxStringBuilder implements QlBuilder<This> {

    public AbstractQlBuilder(Syntax syntax, MiDBString dBString) {
        super(syntax, dBString);
    }

    public AbstractQlBuilder(Syntax syntax, StatementBuilder stmt) {
        super(syntax, stmt.begin(MiDBString.TYPE));
    }

    protected MiDBString getWriteDelegate() {
        closeNestedClause();
        return getDBString();
    }

    @Override
    public This append(CharSequence query) {
        getWriteDelegate().append(query);
        return (This) this;
    }

    @Override
    public This constant(Object key) {
        getSyntax().appendConstant(key, this);
        return (This) this;
    }

    @Override
    public This pushArgument(Object arg) {
        getWriteDelegate().pushArgument(arg);
        return (This) this;
    }
}
