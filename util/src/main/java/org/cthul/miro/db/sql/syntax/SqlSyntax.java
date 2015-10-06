package org.cthul.miro.db.sql.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SelectQueryImpl;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public interface SqlSyntax extends Syntax {
    
    default SelectQuery newSelectQuery(MiConnection cnn) {
        return new SelectQueryImpl(cnn, this);
    }

    @Override
    public default <Req> Req newStatement(MiConnection connection, RequestType<Req> type, RequestType<Req> onDefault) {
        switch (SqlDQML.type(type)) {
            case SELECT:
                return type.cast(newSelectQuery(connection));
        }
        return onDefault.createDefaultRequest(this, connection);
    }
    
    QlBuilder<?> newQlBuilder(CoreStmtBuilder coreBuilder);

    @Override
    default <Cls> Cls newClause(CoreStmtBuilder coreBuilder, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        if (type == QlBuilder.TYPE) {
            return type.cast(newQlBuilder(coreBuilder));
        }
//        switch (SqlClause.type(type)) {
//            case SELECT:
//                return type.cast(newSelect(parent));
//        }
        return onDefault.createDefaultClause(this, coreBuilder);
    }
}
