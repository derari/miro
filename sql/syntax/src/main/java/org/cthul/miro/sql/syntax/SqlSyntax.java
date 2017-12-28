package org.cthul.miro.sql.syntax;

import org.cthul.miro.db.impl.AbstractNestedBuilder;
import org.cthul.miro.sql.CreateStatement;
import org.cthul.miro.sql.InsertStatement;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.SqlClause.Conjunction;
import org.cthul.miro.sql.SqlClause.Junction;
import org.cthul.miro.sql.SqlDDL;
import org.cthul.miro.sql.impl.StandardSelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.sql.impl.StandardCreateStatement;
import org.cthul.miro.sql.impl.StandardInsertStatement;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public interface SqlSyntax extends Syntax {
    
    default SelectQuery newSelectQuery(MiDBString dbString, MiQueryString owner) {
        return new StandardSelectQuery(this, dbString, owner);
    }

    default InsertStatement newInsertStatement(MiDBString dbString, MiUpdateString owner) {
        return new StandardInsertStatement(this, dbString, owner);
    }
    
    default CreateStatement newCreateStatement(MiDBString dbString, MiUpdateString  owner) {
        return new StandardCreateStatement(this, dbString, owner);
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
        return new StandardJunction<>(owner, dbString, this);
    }
    
    default <O> SqlClause.Conjunction<O> newConjunction(MiDBString dbString, O owner) {
        return new StandardConjunction<>(owner, dbString, this);
    }
    
    @Override
    default <Cls> Cls newClause(MiDBString dbString, Object owner, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        if (type == QlBuilder.TYPE) {
            return type.cast(newQlBuilder(dbString));
        }
        MiQueryString qry = owner instanceof MiQueryString ? (MiQueryString) owner : null;
        MiUpdateString stmt = owner instanceof MiUpdateString ? (MiUpdateString) owner : null;
        switch (SqlDQML.type(type)) {
            case SELECT:
                return type.cast(newSelectQuery(dbString, qry));
            case UPDATE:
                return type.cast(newInsertStatement(dbString, stmt));
        }
        switch (SqlClause.type(type)) {
            case IN:
                return type.cast(newIn(dbString, owner));
            case IS_NULL:
                return type.cast(newIsNull(dbString, owner));
            case CONJUNCTION:
                return type.cast(newConjunction(dbString, owner));
            case JUNCTION:
                return type.cast(newJunction(dbString, owner));
        }
        switch (SqlDDL.type(type)) {
            case CREATE_TABLE:
                return type.cast(newCreateStatement(dbString, stmt));
        }
        return Syntax.super.newClause(dbString, owner, type, onDefault);
    }
    
    abstract class SimpleComposite<Owner, This extends QlBuilder<This>> extends AbstractNestedBuilder<Owner, This> {
        
        private boolean writeOP = true;

        public SimpleComposite(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            this(owner, syntax.asQlBuilder(dbString), (Syntax) syntax);
        }

        public SimpleComposite(Owner owner, QlBuilder<?> builder, Syntax syntax) {
            super(owner, builder, syntax);
        }

        @Override
        protected void open() {
            writeOP = false;
            ql("((");
            super.open();
        }

        @Override
        protected QlBuilder<?> getWriteDelegate() {
            QlBuilder<?> sup = super.getWriteDelegate();
            if (writeOP) {
                writeOP = false;
                ql(") ");
                writeOP();
                ql(" (");
            }
            return sup;
        }
        
        protected abstract void writeOP();

        protected This next() {
            writeOP = true;
            return _this();
        }

        @Override
        public void close() {
            writeOP = false;
            super.close();
            ql("))");
        }
    }
    
    class StandardConjunction<Owner> extends SimpleComposite<Owner, Conjunction<Owner>> implements Conjunction<Owner> {

        public StandardConjunction(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, dbString, syntax);
        }

        public StandardConjunction(Owner owner, QlBuilder<?> builder, Syntax syntax) {
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
    
    class StandardJunction<Owner> extends SimpleComposite<Owner, Junction<Owner>> implements Junction<Owner> {

        public StandardJunction(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, dbString, syntax);
        }

        public StandardJunction(Owner owner, QlBuilder<?> builder, Syntax syntax) {
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
