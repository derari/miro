package org.cthul.miro.sql.composer.model;

import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SqlJoinableClause;
import org.cthul.miro.sql.composer.SqlDqmlComposer;

/**
 *
 */
public class JoinedVirtualView extends ViewComposerBase {
    
    private final QlCode aliasPrefix;
    private final QlCode onCondition;

    public JoinedVirtualView(String aliasPrefix, QlCode onCondition, SqlTemplates owner) {
        this(QlCode.ql(aliasPrefix + "."), onCondition, owner);
    }

    protected JoinedVirtualView(QlCode aliasPrefix, QlCode onCondition, SqlTemplates owner) {
        super(owner);
        this.aliasPrefix = aliasPrefix;
        this.onCondition = onCondition;
    }

    protected JoinedVirtualView(JoinedVirtualView source) {
        super(source);
        this.aliasPrefix = source.aliasPrefix;
        this.onCondition = source.onCondition;
    }

    @Override
    protected SqlSnippet<? super SelectBuilder> getSnippet(String key) {
        SqlAttribute at = getOwner().getAttributes().get(key);
        if (at != null) {
            return (SqlSnippet) at.getSelectSnippet(aliasPrefix);
        }
        SqlTable tb = getOwner().getTables().get(key);
        if (tb instanceof SqlTable.From) {
            return ((SqlTable.From) tb).getJoinSnippet(SqlJoinableClause.JoinType.INNER, onCondition);
        }
        return (SqlSnippet) getOwner().getSelectSnippet(key);
    }

    @Override
    protected Initializable<SqlDqmlComposer> copyInstance() {
        return new JoinedVirtualView(this);
    }
}
