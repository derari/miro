package org.cthul.miro.query.template;

import java.util.Objects;

public class CombinedKey {

    private final Object key1;
    private final Object key2;
    private int hash = -1;

    public CombinedKey(Object key1, Object key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public int hashCode() {
        if (hash != -1) return hash;
        hash = 5;
        hash = 59 * hash + Objects.hashCode(this.key1);
        hash = 59 * hash + Objects.hashCode(this.key2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CombinedKey other = (CombinedKey) obj;
        if (!Objects.equals(this.key1, other.key1)) {
            return false;
        }
        if (!Objects.equals(this.key2, other.key2)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return key1 + "/" + key2;
    }
}
