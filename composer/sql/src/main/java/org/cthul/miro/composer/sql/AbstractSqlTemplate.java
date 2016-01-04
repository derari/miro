package org.cthul.miro.composer.sql;

import java.util.LinkedHashSet;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.AbstractTemplate;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractSqlTemplate<Builder> extends AbstractTemplate<Builder> {
    
    private final SqlTemplates owner;

    public AbstractSqlTemplate(SqlTemplates owner, Template<? super Builder> parent) {
        super(parent);
        this.owner = owner;
    }

    public SqlTemplates getOwner() {
        return owner;
    }
    
    protected abstract class AbstractAttributeList implements SqlQueryKey.AttributeList {

        private final LinkedHashSet<Attribute> attributes = new LinkedHashSet<>();
        private final InternalComposer<?> ic;

        public AbstractAttributeList(InternalComposer<?> ic) {
            this.ic = ic;
        }

        @Override
        public void enter(ComposerKey.Phase phase) {
            if (phase == ComposerKey.Phase.BUILD && attributes.isEmpty()) {
                ic.require(SqlQueryKey.DEFAULT_ATTRIBUTES);
            }
        }

        public LinkedHashSet<Attribute> getAttributes() {
            return attributes;
        }

        @Override
        public void add(Attribute attribute) {
            if (attributes.add(attribute)) {
                if (!attribute.isInternal()) {
                    ic.optional(ComposerKey.RESULT, r -> r.add(attribute.getKey()));
                }
            }
        }
    }
}
