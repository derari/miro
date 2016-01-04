package org.cthul.miro.db.sql.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.impl.AbstractNestedBuilder;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SqlClause;
import org.cthul.miro.db.sql.SqlClause.Conjunction;
import org.cthul.miro.db.sql.SqlClause.Junction;
import org.cthul.miro.db.sql.impl.SimpleSelectQuery;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public interface SqlSyntax extends Syntax {
    
    default SelectQuery newSelectQuery(MiConnection cnn) {
        return new SimpleSelectQuery(cnn, this);
    }

    @Override
    public default <Req> Req newStatement(MiConnection connection, RequestType<Req> type, RequestType<Req> onDefault) {
        switch (SqlDQML.type(type)) {
            case SELECT:
                return type.cast(newSelectQuery(connection));
        }
        return onDefault.createDefaultRequest(this, connection);
    }
    
    default QlBuilder<?> asQlBuilder(MiDBString dbString) {
        if (dbString instanceof QlBuilder) {
            return (QlBuilder) dbString;
        } else {
            return newQlBuilder(dbString);
        }
    }
    
    QlBuilder<?> newQlBuilder(MiDBString dbString);
    
    <O> SqlClause.IsNull<O> newIsNull(MiDBString dbString, O owner);
    
    <O> SqlClause.In<O> newIn(MiDBString dbString, O owner);
    
    default <O> SqlClause.Junction<O> newJunction(MiDBString dbString, O owner) {
        return new SimpleJunction<>(owner, dbString, this);
    }
    
    default <O> SqlClause.Conjunction<O> newConjunction(MiDBString dbString, O owner) {
        return new SimpleConjunction<>(owner, dbString, this);
    }
    
    @Override
    default <Cls> Cls newClause(MiDBString dbString, Object owner, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        if (type == QlBuilder.CLAUSE) {
            return type.cast(newQlBuilder(dbString));
        }
        if (type instanceof SqlClause.Clauses) {
            switch ((SqlClause.Clauses) type) {
                case IN:
                    return type.cast(newIn(dbString, owner));
                case IS_NULL:
                    return type.cast(newIsNull(dbString, owner));
            }
        }
        return onDefault.createDefaultClause(this, dbString);
    }
    
    abstract class SimpleComposite<Owner, This extends QlBuilder<This>> extends AbstractNestedBuilder<Owner, This> {
        
        private boolean first = true;
        private boolean writeOP = true;

        public SimpleComposite(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            this(owner, syntax.asQlBuilder(dbString), (Syntax) syntax);
        }

        public SimpleComposite(Owner owner, QlBuilder<?> builder, Syntax syntax) {
            super(owner, builder, syntax);
        }

        @Override
        protected QlBuilder<?> getDelegatee() {
            if (writeOP) {
                writeOP = false;
                if (first) {
                    first = false;
                    ql("((");
                } else {
                    ql(") ");
                    writeOP();
                    ql("( ");
                }
            }
            return super.getDelegatee();
        }
        
        protected abstract void writeOP();

        protected This next() {
            writeOP = true;
            return _this();
        }

        @Override
        public Owner end() {
            writeOP = false;
            if (!first) {
                ql("))");
            }
            return super.end();
        }
    }
    
    class SimpleConjunction<Owner> extends SimpleComposite<Owner, Conjunction<Owner>> implements Conjunction<Owner> {

        public SimpleConjunction(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, dbString, syntax);
        }

        public SimpleConjunction(Owner owner, QlBuilder<?> builder, Syntax syntax) {
            super(owner, builder, syntax);
        }

        @Override
        public Conjunction<Owner> and() {
            return next();
        }

        @Override
        protected void writeOP() {
            ql("AND");
        }
    }
    
    class SimpleJunction<Owner> extends SimpleComposite<Owner, Junction<Owner>> implements Junction<Owner> {

        public SimpleJunction(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, dbString, syntax);
        }

        public SimpleJunction(Owner owner, QlBuilder<?> builder, Syntax syntax) {
            super(owner, builder, syntax);
        }

        @Override
        public Junction<Owner> or() {
            return next();
        }

        @Override
        protected void writeOP() {
            ql("OR");
        }
    }
}
