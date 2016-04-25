package org.cthul.miro.migrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public final class Version implements Comparable<Version> {
    
    private final Object[] id;
    private final String str;

    public Version(Object[] id, String str) {
        this.id = id;
        if (id == null) throw new NullPointerException();
        this.str = str;
    }
    
    protected Version(Version source) {
        this.id = source.id;
        this.str = source.str;
    }

    public Version(String version) {
        this(parse(version));
    }

    @Override
    public int compareTo(Version o) {
        int len = Math.min(id.length, o.id.length);
        for (int i = 0; i < len; i++) {
            int c = cmp(id[i], o.id[i]);
            if (c != 0) return c;
        }
        return id.length - o.id.length;
    }
    
    private int cmp(Object o1, Object o2) {
        if (o1 instanceof Number) {
            if (o2 instanceof Number) {
                return Long.compare(
                        ((Number) o1).longValue(),
                        ((Number) o2).longValue());
            } else {
                return Long.compare(((Number) o1).longValue(), 0);
            }
        }
        if (o2 instanceof Number) {
            return Long.compare(0, ((Number) o2).longValue());
        }
        return o1.toString().compareTo(o2.toString());
    }

    @Override
    public String toString() {
        Iterable<String> it = () -> Arrays.asList(id).stream().map(Object::toString).iterator();
        return String.join("-", it);
    }

    @Override
    public int hashCode() {
        return 2938426 ^ Arrays.deepHashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        return Arrays.deepEquals(this.id, other.id);
    }
    
    public static Version parse(String version) {
        StringBuilder cleanString = new StringBuilder();
        Object[] id = parseString(version, cleanString);
        return new Version(id, cleanString.toString());
    }
    
    private static Object[] parseString(String version, StringBuilder cleanString) {
        String[] parts = SEP.split(version);
        List<Object> result = new ArrayList<>(parts.length);
        for (String part: parts) {
            if (DIGITS.matcher(part).matches()) {
                cleanString.append(part).append("-");
                result.add(Long.parseLong(part));
            } else if (!part.isEmpty()) {
                cleanString.append(part).append("-");
                result.add(part);
            }
        }
        if (parts.length > 0) {
            cleanString.setLength(cleanString.length() - 1);
        }
        return result.toArray();
    }
    
    private static final Pattern SEP = Pattern.compile("[-_.:,;/\\\\]+");
    private static final Pattern DIGITS = Pattern.compile("\\d+");
}
