package org.cthul.miro.sql.syntax;

import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.db.request.MiUpdateBuilder;
import org.cthul.miro.db.request.StatementBuilder;
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
import org.cthul.miro.db.string.AbstractNestedBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public interface SqlSyntax extends Syntax {
    
    default SelectQuery newSelectQuery(StatementBuilder parent) {
        MiQueryBuilder query = (parent instanceof MiQueryBuilder) ? (MiQueryBuilder) parent : null;
        return new StandardSelectQuery(this, parent, query);
    }

    default InsertStatement newInsertStatement(StatementBuilder parent) {
        MiUpdateBuilder update = (parent instanceof MiUpdateBuilder) ? (MiUpdateBuilder) parent : null;
        return new StandardInsertStatement(this, parent, update);
    }
    
    default CreateStatement newCreateStatement(StatementBuilder parent) {
        MiUpdateBuilder update = (parent instanceof MiUpdateBuilder) ? (MiUpdateBuilder) parent : null;
        return new StandardCreateStatement(this, parent, update);
    }
    
    QlBuilder<?> newQlBuilder(StatementBuilder stmt);
    
    SqlClause.OpenIsNull newIsNull(StatementBuilder stmt);
    
    SqlClause.OpenIn newIn(StatementBuilder stmt);
    
    @SuppressWarnings("Convert2Lambda")
    default SqlClause.OpenJunction newJunction(StatementBuilder stmt) {
        return new SqlClause.OpenJunction() {
            @Override
            public <T> Junction<T> open(T parent) {
                return stmt.as(nested -> new StandardJunction<>(parent, nested, SqlSyntax.this));
            }
        };
    }
    
    @SuppressWarnings("Convert2Lambda")
    default SqlClause.OpenConjunction newConjunction(StatementBuilder stmt) {
        return new SqlClause.OpenConjunction() {
            @Override
            public <T> Conjunction<T> open(T parent) {
                return stmt.as(nested -> new StandardConjunction<>(parent, nested, SqlSyntax.this));
            }
        };
    }

    @Override
    default <Cls> Cls newClause(StatementBuilder stmt, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        if (type == QlBuilder.TYPE) {
            return type.cast(newQlBuilder(stmt));
        }
        switch (SqlDQML.type(type)) {
            case SELECT:
                return type.cast(newSelectQuery(stmt));
            case UPDATE:
                return type.cast(newInsertStatement(stmt));
        }
        switch (SqlClause.type(type)) {
            case IN:
                return type.cast(newIn(stmt));
            case IS_NULL:
                return type.cast(newIsNull(stmt));
            case CONJUNCTION:
                return type.cast(newConjunction(stmt));
            case JUNCTION:
                return type.cast(newJunction(stmt));
        }
        switch (SqlDDL.type(type)) {
            case CREATE_TABLE:
                return type.cast(newCreateStatement(stmt));
        }
        return Syntax.super.newClause(stmt, type, onDefault);
    }
    
    abstract class SimpleComposite<Owner, This extends QlBuilder<This>> extends AbstractNestedBuilder<Owner, This> {
        
        private boolean writeOP = true;

        public SimpleComposite(Owner owner, StatementBuilder stmt, SqlSyntax syntax) {
            this(owner, stmt.begin(QlBuilder.TYPE), (Syntax) syntax);
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

        public StandardConjunction(Owner owner, StatementBuilder stmt, SqlSyntax syntax) {
            super(owner, stmt, syntax);
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

        public StandardJunction(Owner owner, StatementBuilder stmt, SqlSyntax syntax) {
            super(owner, stmt, syntax);
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
