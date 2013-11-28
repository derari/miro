package org.cthul.miro.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

    public static String[][] parseSelectClause(String select) {
        List<String> parts = splitSelectClause(select);
        final String[][] result = new String[parts.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = parseSelectPart(parts.get(i));
        }
        return result;
    }
    
    public static String[][] parseAttributes(String select) {
        List<String> parts = splitSelectClause(select);
        final String[][] result = new String[parts.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = parseAttributePart(parts.get(i));
        }
        return result;
    }
    
    protected static List<String> splitSelectClause(String select) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < select.length(); i++) {
            int start = i;
            i = scanUntil(i, select, ',');
            parts.add(select.substring(start, i).trim());
        }
        return parts;
    }
    
    private static final char[] BR_OPEN =  {'(', '[', '{'};
    private static final char[] BR_CLOSE = {')', ']', '}'};
    private static final char[] QUOTE = {'\'', '"', '`'};
    private static final char ESCAPE = '\\';
    
    private static String QuotedString(String q) {
        return "q(?:[^q]|\\\\[\\\\q])*q".replace("q", q);
    }
    
    private static Pattern PartPattern(String p) {
        return Pattern.compile(
                p.replace("_", "\\s*")
                 .replace("~", "\\s+")
                 .replace("IDENT", P_IDENT),
                Pattern.CASE_INSENSITIVE);
    }
    
    private static final String P_IDENT = "(?:[_$a-zA-Z0-9]+|"
            + QuotedString("'") + "|" + QuotedString("\"") + "|"
            + QuotedString("`") + ")";
    
    private static final Pattern SELECT_PART_PATTERN = PartPattern(
            "(((?:(IDENT)_\\._)?(IDENT))(?:_AS_(IDENT))?)"
             + "|" +
             "((.+?)AS_(IDENT))");
    
    public static String[] parseSelectPart(String select) {
        select = select.trim();
        Matcher m = SELECT_PART_PATTERN.matcher(select);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + select);
        }
//        final String key, required;
//        if (m.group(1) != null) {
//            key = m.group(5) != null ? m.group(5) : m.group(3);
//            required = m.group(2);
//        } else if (m.group(6) != null) {
//            key = m.group(7);
//            required = null;
//        } else {
//            throw new AssertionError(m.toString());
//        }
//        return new String[]{stripQuotes(key), select, stripQuotes(required)};
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + select);
        }
        final String key, def, table/*, column*/;
        if (m.group(1) != null) {
            def = m.group(2);
            table = m.group(3);
            //column = m.group(4);
            key = m.group(5) != null ? m.group(5) : m.group(4);
        } else {
            def = m.group(7).trim();
            key = m.group(8);
            table = null;
            //column = null;
        }
        return new String[]{stripQuotes(key), def, stripQuotes(table), key};
    }
    
    private static final Pattern ATTRIBUTE_PART_PATTERN = PartPattern(
            "((?:(IDENT)_\\._)?(IDENT))(?:_AS_(IDENT))?");
    
    public static String[] parseAttributePart(String attribute) {
        attribute = attribute.trim();
        Matcher m = ATTRIBUTE_PART_PATTERN.matcher(attribute);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + attribute);
        }
        final String key, def, column, table;
        def = m.group(1);
        table = m.group(2);
        column = m.group(3);
        key = m.group(4) != null ? m.group(4) : column;
        return new String[]{stripQuotes(key), def, stripQuotes(table), key, column};
    }
    
    private static final Pattern FROM_PART_PATTERN = PartPattern(
            "(.*?)(IDENT)");
    
    public static String[] parseFromPart(String from) {
        Matcher m = FROM_PART_PATTERN.matcher(from.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + from);
        }
        final String key, table;
        if (m.group(2) != null) {
            key = stripQuotes(m.group(2));
        } else {
            throw new AssertionError(m.toString());
        }
        if (m.group(1).isEmpty()) {
            table = m.group(2);
        } else {
            table = m.group(1);
        }
        return new String[]{key, from, table.trim()};
    }
    
    private static final Pattern JOIN_PART_PATTERN = PartPattern(
            "((?:RIGHT~OUTER|LEFT~OUTER|INNER)?_JOIN)?.*?(IDENT)(_ON.*)?");
    
    public static String[] parseJoinPart(String join) {
        Matcher m = JOIN_PART_PATTERN.matcher(join.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + join);
        }
        final String key;
        if (m.group(2) != null) {
            key = m.group(2);
            if (m.group(1) == null) {
                join = "JOIN " + join;
            }
        } else {
            throw new AssertionError(m.toString());
        }
        return new String[]{stripQuotes(key), join};
    }

    public static String[] parseGroupPart(String from) {
        Matcher m = GROUP_PART_PATTERN.matcher(from.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + from);
        }
        final String key;
        if (m.group(2) != null) {
            key = m.group(2);
        } else {
            throw new AssertionError(m.toString());
        }
        return new String[]{stripQuotes(key), from};
    }
    
    public static String[] parseOrderPart(String orderBy) {
        Matcher m = ORDER_PART_PATTERN.matcher(orderBy.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse: " + orderBy);
        }
        final String key, required;
        if (m.group(3) != null) {
            key = m.group(3);
        } else {
            throw new AssertionError(m.toString());
        }
        if (m.group(2) != null) {
            required = m.group(2);
        } else {
            required = null;
        }
        return new String[]{stripQuotes(key), orderBy, stripQuotes(required)};
    }
    
    private static final Pattern GROUP_PART_PATTERN = PartPattern(
            "(IDENT_\\._)?(IDENT)");
    
    private static final Pattern ORDER_PART_PATTERN = PartPattern(
            "((IDENT)_\\._)?(IDENT)(_ASC|_DESC)?");
    
    private static int scanUntil(int i, String select, char end) {
        for (;i < select.length(); i++) {
            char c = select.charAt(i);
            if (c == end) {
                return i;
            } else {
                for (int br = 0; br < BR_OPEN.length; br++) {
                    if (c == BR_OPEN[br]) {
                        i = scanUntil(i+1, select, BR_CLOSE[br]);
                    }
                }
                for (char q: QUOTE) {
                    if (c == q) {
                        i = scanQuote(i+1, select, q);
                        break;
                    }
                }
            }
        }
        return i;
    }
    
    private static int scanQuote(int i, String select, char q) {
        for (;i < select.length(); i++) {
            char c = select.charAt(i);
            if (c == q) {
                return i;
            } else if (c == ESCAPE) {
                i++;
            }
        }
        return i;
    }
    
    private static String stripQuotes(String s) {
        if (s == null || s.isEmpty()) return s;
        char c0 = s.charAt(0);
        for (char q: QUOTE) {
            if (c0 == q) {
                if (s.length() < 2 || s.charAt(s.length()-1) != q) {
                    throw new IllegalArgumentException("Illegal quotes: " + s);
                }
                return s.substring(1, s.length()-1);
            }
        }
        return s;
    }
}
