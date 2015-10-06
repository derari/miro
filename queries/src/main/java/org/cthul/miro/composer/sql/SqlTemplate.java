package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.QueryKey;
import static org.cthul.miro.composer.QueryParts.*;
import org.cthul.miro.composer.template.AbstractTemplate;
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
        switch (QueryKey.key(key)) {
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
}
