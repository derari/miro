package org.cthul.miro.query.parts;

public interface ConfigurationQueryPart extends QueryPart {

    Object getConfiguration();
    
    Object[] getArguments();
}
