package org.cthul.miro.map;

import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.composer.template.AbstractTemplate;
import org.cthul.miro.composer.template.Template;

/**
 *
 * @param <Entity>
 */
public class MappedTemplateBuilder<Entity> {
    
    private final Map<String, Field> fields = new HashMap<>();

    public MappedTemplateBuilder() {
    }

    public Map<String, Field> getFields() {
        return fields;
    }
    
    public <Builder extends EntitySetup<? extends Entity>> Template<Builder> buildSelectTemplate(Template<? super Builder> parent) {
        return new SelectTemplate<>(parent);
    }
    
    protected static class SelectTemplate<Entity, Builder extends EntitySetup<? extends Entity>>
                     extends AbstractTemplate<Builder> {
        public SelectTemplate(Template<? super Builder> parent) {
            super(parent);
        }
        @Override
        protected Template<? super Builder> createPartType(Object key) {
            return null;
        }
    }
}
