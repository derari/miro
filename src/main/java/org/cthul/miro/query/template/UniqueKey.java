package org.cthul.miro.query.template;

public class UniqueKey {
    
    private final String name;

    public UniqueKey(String name) {
        this.name = name + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public String toString() {
        return name;
    }
}
