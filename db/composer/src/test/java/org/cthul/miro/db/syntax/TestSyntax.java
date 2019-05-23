package org.cthul.miro.db.syntax;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.AbstractNestedBuilder;
import org.cthul.miro.db.string.AbstractQlBuilder;
import org.cthul.miro.db.string.MiDBString;

/**
 *
 */
public class TestSyntax implements Syntax {

    @Override
    public <Cls> Cls newClause(StatementBuilder stmt, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        Object cls = null;
        if (type == QlBuilder.TYPE) {
            cls = new Ql(this, stmt);
        } else if (type == IN_PARENTHESES) {
            cls = new InParenImpl(stmt, this);
        }
        return cls != null ? type.cast(cls) : onDefault.createDefaultClause(this, stmt);
    }
    
    public static ClauseType<InParentheses> IN_PARENTHESES = new ClauseType<InParentheses>() { };
    
    public static interface InParentheses extends QlBuilder<InParentheses> {
        
    }
    
    static class InParenImpl extends AbstractNestedBuilder<Object, InParentheses> implements InParentheses {

        public InParenImpl(StatementBuilder parent, Syntax syntax) {
            super(null, parent, syntax);
        }

        @Override
        protected void open() {
            super.open();
            ql("(");
        }

        @Override
        public void close() {
            ql(")");
            super.close();
        }
    }
    
    static class Ql extends AbstractQlBuilder<Ql> {

        public Ql(Syntax syntax, StatementBuilder stmt) {
            super(syntax, stmt);
        }

        @Override
        public Ql identifier(String id) {
            ql("`").ql(id).ql("`");
            return this;
        }

        @Override
        public Ql stringLiteral(String string) {
            ql("'").ql(string).ql("'");
            return this;
        }
    }
}
