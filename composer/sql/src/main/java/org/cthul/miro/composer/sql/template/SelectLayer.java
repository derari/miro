package org.cthul.miro.composer.sql.template;

import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.sql.SqlComposerKey;
import org.cthul.miro.composer.sql.SqlSnippet;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.db.sql.SelectBuilder;

/**
 *
 * @param <Builder>
 */
public class SelectLayer<Builder extends SelectBuilder> extends AbstractSqlLayer<Builder> {
    
    public SelectLayer(SqlTemplates owner) {
        super(owner);
    }

    @Override
    protected String getShortString() {
        return "SQL-Select";
    }

    @Override
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        if (key instanceof String) {
            String sKey = (String) key;
            if (getOwner().getAttributes().containsKey(sKey)) {
                return Templates.setUp(SqlComposerKey.ATTRIBUTES, at -> at.add(sKey));
            }
        }
        if (key == getOwner().getMainKey()) {
            return Templates.newNode(ic -> new SelectView(ic));
        }
        return null;
    }
    
    protected class SelectView extends ViewComposerBase {

        public SelectView(InternalComposer<? extends Builder> composer) {
            super(composer);
        }

        public SelectView(InternalComposer<? extends Builder> ic, SelectView source) {
            super(ic, source);
        }

        @Override
        public Object copyFor(InternalComposer<Builder> ic) {
            return new SelectView(ic, this);
        }

        @Override
        protected SqlSnippet<? super Builder> getSnippet(String key) {
            return getOwner().getSelectSnippet(key);
        }
    }
}
