package org.cthul.miro.dml;

public enum DataQuerySubkey {
    
    DEFAULT,
    
    ADD,
    ADD_ALL,
    
    AS,
    
    ASC,
    DESC,
    
    UNKOWN;
    
    public static DataQuerySubkey asDataQuerySubkey(Object o) {
        if (o instanceof DataQuerySubkey) {
            return (DataQuerySubkey) o;
        }
        if (o == null) {
            return DEFAULT;
        }
        if (o instanceof String) {
            switch ((String) o) {
                case "asc":
                    return ASC;
                case "desc":
                    return DESC;
            }
        }
        return UNKOWN;
    }
    
}
