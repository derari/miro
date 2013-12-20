package org.cthul.miro.dml;

import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.sql.DataQueryPart;

public enum DataQueryKey {
    
    PUT_ALWAYS,
    /** Includes all parts that are flagged as default. 
     * @see #ATTRIBUTE */
    PUT_DEFAULT,
    /** Includes all parts that are flagged as default or optional. 
     * @see #ATTRIBUTE */
    PUT_OPTIONAL,
    /** Includes all key attributes 
     * @see #ATTRIBUTE */
    PUT_KEYS,
    /** Expects a comma-separated list of strings, which will be put as 
     * attributes or regular keys.
     * @see #ATTRIBUTE */
    PUT_STRINGS,
//    /** Includes all generated key attributes 
//     * @see #ATTRIBUTE */
//    INCLUDE_GENERATED_KEYS,
    /** Puts {@link #ATTRIBUTE_DEPENDENCIES} for all keys. */
    ALL_KEY_DEPENDENCIES,
    
    /** Puts all dependencies of an attribute into the query. */
    ATTRIBUTE_DEPENDENCIES,
    /** Puts an attribute and its dependencies into the query.
     * For Select, it will be part of the select clause; for Insert and Update,
     * it will be written to the database.
     * @see DataQueryPart#ATTRIBUTE */
    INCLUDE_ATTRIBUTE,
    /** Adds an attribute to the result list.
     * @see InternalQueryBuilder#addResultAttribute(java.lang.String) */
    ADD_TO_RESULT,
    ADD_GENERATED_KEYS_TO_RESULT,
    
    /** Depending on the query type, proxy to {@link #SELECT}, {@link #INSERT},
     * {@link #UPDATE}, or {@link #DELETE}.
     * Usually puts {@link #INCLUDE_ATTRIBUTE}.
     * Attribute names are redirected to this.
     */
    ATTRIBUTE,
    /** {@linkplain #INCLUDE_ATTRIBUTE Includes} attribute and 
     * {@linkplain #ADD_TO_RESULT adds to result}. */
    SELECT,
    /** {@linkplain #INCLUDE_ATTRIBUTE Includes} attribute. */
    INSERT,
    /** {@linkplain #INCLUDE_ATTRIBUTE Includes} attribute. */
    UPDATE,
    /** Not allowed. */
    DELETE,
    
    /** For Update and Insert, the given attribute will be set to a fixed argument */
    SET_ATTRIBUTE,
    
    /** Depending on the query type, proxy to {@link #SELECT_VALUES}, 
     * {@link #INSERT_VALUES}, {@link #UPDATE_VALUES}, or {@link #DELETE_VALUES}.
     * Puts values into the query. {@link DataQuerySubkey#ADD ADD} or 
     * {@link DataQuerySubkey#ADD_ALL ADD_ALL} should be used.
     * @see DataQueryPart#VALUES */
    VALUES,
    /** Redirects to {@link #KEYS_IN}. */
    SELECT_VALUES,
    /** Provides the values that are inserted into the database. */
    INSERT_VALUES,
    /** Provides the values that will be updated into the database,
     * and their keys. */
    UPDATE_VALUES,
    /** Provides the keys of the tuples that will be deleted. */
    DELETE_VALUES,
    
    /** Adds a filter for keys of the given values. 
     * Update and Delete only. */
    FILTER_BY_KEYS,
    
    KEYS_IN,
    
    GROUP_BY,
    GROUP_BY_KEYS,
    
    ORDER_BY,
    ORDER_BY_KEYS,

    UNKOWN;
    
    public static DataQueryKey asDataQueryKey(Object o) {
        if (o instanceof DataQueryKey) {
            return (DataQueryKey) o;
        }
        if (o instanceof String) {
            DataQueryKey k = KEY_MAP.get((String) o);
            if (k != null) return k;
        }
        return UNKOWN;
    }

    private DataQueryKey() {
    }

    private static final Map<String, DataQueryKey> KEY_MAP = new HashMap<>();
    
    static {
        KEY_MAP.put("*",                PUT_DEFAULT);
        KEY_MAP.put("**",               PUT_OPTIONAL);
        KEY_MAP.put("groupBy-keys",     GROUP_BY_KEYS);
        KEY_MAP.put("orderBy-keys",     ORDER_BY_KEYS);
        for (DataQueryKey k: values()) {
            String s = k.toString();
            if (s.contains("_")) {
                s = s.toLowerCase().replace('_', '-');
                KEY_MAP.put(s, k);
            }
        }
    }
}
