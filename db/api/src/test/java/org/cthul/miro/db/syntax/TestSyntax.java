package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.impl.AbstractNestedBuilder;
import org.cthul.miro.db.impl.AbstractQlBuilder;
import org.cthul.miro.db.stmt.MiDBString;

/**
 *
 */
public class TestSyntax implements Syntax {

    @Override
    public <Req> Req newStatement(MiConnection cnn, RequestType<Req> type, RequestType<Req> onDefault) {
        return onDefault.createDefaultRequest(this, cnn);
    }

    @Override
    public <Cls> Cls newClause(MiDBString stmt, Object owner, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        Object cls = null;
        if (type == QlBuilder.CLAUSE) {
            cls = new Ql(this, stmt);
        } else if (type == IN_PARENT) {
            cls = new InParenImpl(owner, stmt, this);
        }
        return cls != null ? type.cast(cls) : onDefault.createDefaultClause(this, stmt);
    }
    
    public static ClauseType<InParentheses> IN_PARENT = new ClauseType<InParentheses>() { };
    
    public static interface InParentheses extends QlBuilder<InParentheses> {
        
    }
    
    static class InParenImpl extends AbstractNestedBuilder<Object, InParentheses> implements InParentheses {

        public InParenImpl(Object owner, MiDBString dbString, Syntax syntax) {
            super(owner, dbString, syntax);
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

        public Ql(Syntax syntax, MiDBString coreBuilder) {
            super(syntax, coreBuilder);
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
