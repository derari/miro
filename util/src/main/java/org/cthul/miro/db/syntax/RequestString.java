package org.cthul.miro.db.syntax;

import java.util.List;

/**
 *
 */
public interface RequestString extends RequestBuilder<RequestString> {
    
    List<Object> getArguments();
    
//    int length();
    
    @Override
    String toString();
}
