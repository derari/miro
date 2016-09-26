package org.cthul.miro.sql.template;

import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.sql.SqlJoinableClause;
import org.cthul.miro.sql.SqlTableClause;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public class JoinedLayer<Builder extends SqlTableClause & SqlJoinableClause & SqlFilterableClause> extends AbstractSqlLayer<Builder> {

    private final Key<ViewComposer> key;
    private final QlCode aliasPrefix;
    private final QlCode onCondition;
    
    public JoinedLayer(SqlTemplates owner, Key<ViewComposer> key, String prefix, QlCode onCondition) {
        super(owner);
        this.key = key;
        this.aliasPrefix = QlCode.ql(prefix + ".");
        this.onCondition = onCondition;
    }

    @Override
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        if (key == this.key) {
            return Templates.newNode(ic -> new JoinedView(ic));
        }
        return null;
    }
    
    protected class JoinedView extends ViewComposerBase {

        public JoinedView(InternalComposer<? extends Builder> ic) {
            super(ic);
        }

        public JoinedView(InternalComposer<? extends Builder> ic, ViewComposerBase source) {
            super(ic, source);
        }

        @Override
        public Object copyFor(InternalComposer<Builder> ic) {
            return new JoinedView(ic, this);
        }

        @Override
        protected Key<ViewComposer> getKey() {
            return key;
        }

        @Override
        protected SqlSnippet<? super Builder> getSnippet(String key) {
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
    }
}
