package org.cthul.miro.sql.template;

import java.util.Objects;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.util.Key;

/**
 *
 */
public class SnippetKey extends ValueKey<Configurable> {
    
    private final String name;
    
    public SnippetKey(Key<ViewComposer> value, String name) {
        super(value);
        this.name = name;
    }

    public Key<ViewComposer> getViewKey() {
        return (Key) getValue();
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hashCode(name);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return super.equals(obj) && Objects.equals(name, ((SnippetKey) obj).name);
    }
}
