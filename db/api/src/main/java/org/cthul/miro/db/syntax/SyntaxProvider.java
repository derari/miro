package org.cthul.miro.db.syntax;

import java.util.ServiceLoader;

/**
 *
 */
public interface SyntaxProvider {

    <S extends Syntax> S handle(Class<S> expectedType, String dbString);
    
    <S extends Syntax> S handleDefault(Class<S> expectedType, String dbString);
    
    static Syntax find(String dbString) {
        return find(Syntax.class, dbString);
    }
    
    static <S extends Syntax> S find(Class<S> expectedType, String dbString) {
        ServiceLoader<SyntaxProvider> spLoader = ServiceLoader.load(SyntaxProvider.class);
        for (SyntaxProvider sp: spLoader) {
            S syntax = sp.handle(expectedType, dbString);
            if (syntax != null) return syntax;
        }
        for (SyntaxProvider sp: spLoader) {
            S syntax = sp.handleDefault(expectedType, dbString);
            if (syntax != null) return syntax;
        }
        throw new IllegalArgumentException("Unknown syntax");
    }
}
