package org.cthul.miro.db.syntax;

public interface OpenClause {
    
    <T> Object open(T parent);
}
