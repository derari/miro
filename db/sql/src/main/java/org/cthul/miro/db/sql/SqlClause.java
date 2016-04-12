package org.cthul.miro.db.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.cthul.miro.db.syntax.NestedBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface SqlClause {
    
    interface Composite<This extends Composite<This>> extends SqlClause, SqlBuilder<This> {
        
        This and();
    }
    
    interface BooleanExpression<This extends BooleanExpression<This>> extends SqlClause, SqlBuilder<This> {
        
        default Junction either() {
            return begin(SqlClause.either());
        }
        
        default This either(Consumer<? super Junction> action) {
            return clause(SqlClause.either(), action);
        }
        
        default Conjunction all() {
            return begin(SqlClause.all());
        }
        
        default This all(Consumer<? super Conjunction> action) {
            return clause(SqlClause.all(), action);
        }
    }
    
    interface Where<This extends Where<This>> extends Composite<This>, BooleanExpression<This> {
        
    }
    
    interface IsNull<Owner> extends SqlBuilder<IsNull<Owner>>, NestedBuilder<Owner> {
        
    }
    
    interface In<Owner> extends NestedBuilder<Owner> {
        
        In setLength(int length);
        
        default In push(Object argument) {
            return list(argument);
        }
        
        default In list(Stream<?> arguments) {
            return list(arguments.iterator());
        }
        
        default In list(Iterable<?> arguments) {
            return list(arguments.iterator());
        }
        
        default In list(Iterator<?> arguments) {
            In me = this;
            while (arguments.hasNext()) {
                me = me.push(arguments.next());
            }
            return me;
        }
        
        In list(Collection<?> arguments);
        
        default In list(Object... arguments) {
            return list(Arrays.asList(arguments));
        }
    }
    
    interface Junction<Owner> extends BooleanExpression<Junction<Owner>>, NestedBuilder<Owner> {
        
        Junction<Owner> or();
    }
    
    interface Conjunction<Owner> extends Composite<Conjunction<Owner>>, BooleanExpression<Conjunction<Owner>>, NestedBuilder<Owner> {
        
    }
    
    static Type type(Object type) {
        return Key.castDefault(type, Type.NIL);
    }
    
    static ClauseType<In<?>> in() { return Type.IN; }
    static ClauseType<IsNull<?>> isNull() { return Type.IS_NULL; }
    static ClauseType<Junction> either() { return Type.JUNCTION; }
    static ClauseType<Conjunction> all() { return Type.CONJUNCTION; }
    
    enum Type implements ClauseType {
        IS_NULL,
        IN,
        JUNCTION,
        CONJUNCTION,
        
        NIL;
    }
}
