package org.cthul.miro.composer.sql;

import java.util.LinkedHashSet;
import org.cthul.miro.composer.QueryComposerKey;
import static org.cthul.miro.composer.QueryParts.*;
import org.cthul.miro.composer.template.AbstractTemplate;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.QueryPartType;
import org.cthul.miro.composer.template.Template;

/**
 *
 */
public abstract class SqlTemplate<Builder> extends AbstractTemplate<Builder> {
    
    private final SqlTemplateBuilder owner;

    public SqlTemplate(SqlTemplateBuilder owner, Template<? super Builder> parent) {
        super(parent);
        this.owner = owner;
    }

    @Override
    protected Template<? super Builder> createPartType(Object key) {
        switch (QueryComposerKey.key(key)) {
            case PHASE:
                return noOp();
        }
        QueryPartType<?> t = owner.getTemplates().get(key);
        if (t != null) return (Template) t;
        return null;
    }

    public SqlTemplateBuilder getOwner() {
        return owner;
    }
    
    protected abstract class AbstractAttributeList implements SqlQueryKey.AttributeList {

        private final LinkedHashSet<Attribute> attributes = new LinkedHashSet<>();
        private final InternalQueryComposer<?> query;

        public AbstractAttributeList(InternalQueryComposer<?> query) {
            this.query = query;
        }

        @Override
        public void enter(QueryComposerKey.Phase phase) {
            if (phase == QueryComposerKey.Phase.COMPOSE && attributes.isEmpty()) {
                query.require(SqlQueryKey.DEFAULT_ATTRIBUTES);
            }
        }

        public LinkedHashSet<Attribute> getAttributes() {
            return attributes;
        }

        @Override()
        public void addAttribute(Object attributeKey) {
            Attribute at = asAttribute(attributeKey);
            if (at == null) {
                throw new IllegalArgumentException(String.valueOf(attributeKey));
            }
            attributes.add(at);
        }

        @SuppressWarnings(value = "element-type-mismatch")
        private Attribute asAttribute(Object a) {
            Attribute at;
            if (a instanceof Attribute) {
                at = (Attribute) a;
            } else {
                at = getOwner().getAttributes().get(a);
            }
            return at;
        }
    }
}
