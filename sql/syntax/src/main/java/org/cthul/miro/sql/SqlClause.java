package org.cthul.miro.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.cthul.miro.db.syntax.NestedBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.OpenClause;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface SqlClause {
    
    interface Composite<This extends Composite<This>> extends SqlClause, SqlBuilder<This> {
        
        This and();
    }
    
    interface BooleanExpression<This extends BooleanExpression<This>> extends SqlClause, SqlBuilder<This> {
        
        default Junction<This> either() {
            return begin(SqlClause.openEither()).open((This) this);
        }
        
        default This either(Consumer<? super Junction<?>> action) {
            return clause(SqlClause.openEither(), newJunction -> action.accept(newJunction.open(null)));
        }
        
        default Conjunction<This> all() {
            return begin(SqlClause.openAll()).open((This) this);
        }
        
        default This all(Consumer<? super Conjunction<?>> action) {
            return clause(SqlClause.openAll(), newConjunction -> action.accept(newConjunction.open(null)));
        }
    }
    
    interface Where<This extends Where<This>> extends Composite<This>, BooleanExpression<This> {
        
    }
    
    interface IsNull<Owner> extends SqlBuilder<IsNull<Owner>>, NestedBuilder<Owner> {
        
    }
    
    interface OpenIsNull extends QlBuilder.Open {

        @Override
        <T> IsNull<T> open(T parent);
    }
    
    interface In<Owner> extends NestedBuilder<Owner> {
        
        In setLength(int length);
        
        default In push(Object argument) {
            return list(argument);
        }
        
        default In list(Object arguments) {
            if (arguments instanceof Collection) {
                return list((Collection) arguments);
            } else if (arguments instanceof Iterable) {
                return list((Iterable) arguments);
            } else if (arguments instanceof Object[]) {
                return list((Object[]) arguments);
            } else if (arguments instanceof Stream) {
                return list((Stream) arguments);
            } else if (arguments instanceof Iterator) {
                return list((Iterator) arguments);
            }
            throw new IllegalArgumentException(String.valueOf(arguments));
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
    
    interface OpenIn extends OpenClause {

        @Override
        <T> In<T> open(T parent);
    }
    
    interface Junction<Owner> extends BooleanExpression<Junction<Owner>>, NestedBuilder<Owner> {
        
        Junction<Owner> or();
    }
    
    interface OpenJunction extends OpenClause {

        @Override
        <T> Junction<T> open(T parent);
    }
    
    interface Conjunction<Owner> extends Composite<Conjunction<Owner>>, BooleanExpression<Conjunction<Owner>>, NestedBuilder<Owner> {
        
    }
    
    interface OpenConjunction extends OpenClause {

        @Override
        <T> Conjunction<T> open(T parent);
    }
    
    static Type type(Object type) {
        return Key.castDefault(type, Type.NIL);
    }
    
    static ClauseType<OpenIn> openIn() { return Type.IN; }
    static ClauseType<OpenIsNull> openIsNull() { return Type.IS_NULL; }
    static ClauseType<OpenJunction> openEither() { return Type.JUNCTION; }
    static ClauseType<OpenConjunction> openAll() { return Type.CONJUNCTION; }
    
    static <T> ClauseType<In<T>> in(T owner) {
        return ClauseType.fromStatement(stmt -> stmt.begin(openIn()).open(owner));
    }
    
    static <T> ClauseType<IsNull<T>> isNull(T owner) {
        return ClauseType.fromStatement(stmt -> stmt.begin(openIsNull()).open(owner));
    }
    
    static <T> ClauseType<Junction<T>> either(T owner) {
        return ClauseType.fromStatement(stmt -> stmt.begin(openEither()).open(owner));
    }
    
    static <T> ClauseType<Conjunction<T>> all(T owner) {
        return ClauseType.fromStatement(stmt -> stmt.begin(openAll()).open(owner));
    }
    
    static ClauseType<In<Void>> in() {
        return Type._IN;
    }
    
    static ClauseType<IsNull<Void>> isNull() {
        return Type._IS_NULL;
    }
    
    static ClauseType<Junction<Void>> either() {
        return Type._JUNCTION;
    }
    
    static ClauseType<Conjunction<Void>> all() {
        return Type._CONJUNCTION;
    }
    
    enum Type implements ClauseType {
        IS_NULL,
        IN,
        JUNCTION,
        CONJUNCTION,
        
        NIL;
        
        static ClauseType<IsNull<Void>> _IS_NULL = isNull(null);
        static ClauseType<In<Void>> _IN = in(null);
        static ClauseType<Junction<Void>> _JUNCTION = either(null);
        static ClauseType<Conjunction<Void>> _CONJUNCTION = all(null);
    }
}
