package org.cthul.miro.dml;

public enum MappedDataQueryKey {
    
    ENTITIES,
    SELECT_ENTITIES,
    INSERT_ENTITIES,
    UPDATE_ENTITIES,
    DELETE_ENTITIES,
    
    UNKNOWN;
    
    public static MappedDataQueryKey asMappedDataQueryKey(Object o) {
        if (o instanceof MappedDataQueryKey) {
            return (MappedDataQueryKey) o;
        }
        return UNKNOWN;
    }
    
}
