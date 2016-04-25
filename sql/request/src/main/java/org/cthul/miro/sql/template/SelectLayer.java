package org.cthul.miro.sql.template;

import java.util.function.Predicate;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.util.Key;

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
        protected Key<ViewComposer> getKey() {
            return getOwner().getMainKey();
        }

        @Override
        protected SqlSnippet<? super Builder> getSnippet(String key) {
            return getOwner().getSelectSnippet(key);
        }

        @Override
        public Object copyFor(InternalComposer<Builder> ic) {
            return new SelectView(ic, this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
}
