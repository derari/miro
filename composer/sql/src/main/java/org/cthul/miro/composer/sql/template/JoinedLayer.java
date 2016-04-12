package org.cthul.miro.composer.sql.template;

import org.cthul.miro.composer.Configurable;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.composer.sql.SqlAttribute;
import org.cthul.miro.composer.sql.SqlSnippet;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.SqlJoinableClause;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public class JoinedLayer<Builder extends SqlJoinableClause & SqlFilterableClause> extends AbstractSqlLayer<Builder> {

    private final Key<MapNode<String, Configurable>> key;
    private final QlCode aliasPrefix;
    
    public JoinedLayer(SqlTemplates owner, Key<MapNode<String, Configurable>> key, String prefix, String condition) {
        super(owner);
        this.key = key;
        this.aliasPrefix = QlCode.ql(prefix);
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
        protected SqlSnippet<? super Builder> getSnippet(String key) {
            SqlAttribute at = getOwner().getAttributes().get(key);
            if (at != null) {
                return (SqlSnippet) at.getSelectSnippet(aliasPrefix);
            }
            return (SqlSnippet) getOwner().getSelectSnippet(key);
        }

        @Override
        public Object copyFor(InternalComposer<Builder> ic) {
            return new JoinedView(ic, this);
        }
    }
}
