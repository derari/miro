package org.cthul.miro.db.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.cthul.miro.db.impl.NestedBuilder;
import org.cthul.miro.db.syntax.ClauseType;

/**
 *
 */
public interface SqlClause {
    
    interface Composite<This extends Composite<This>> extends SqlClause, SqlBuilder<This> {
        
        This and();
    }
    
    interface BooleanExpression<This extends BooleanExpression<This>> extends SqlClause, SqlBuilder<This> {
        
        default Junction either() {
            return begin(EITHER);
        }
        
        default This either(Consumer<? super Junction> action) {
            return clause(EITHER, action);
        }
        
        default Conjunction all() {
            return begin(ALL);
        }
        
        default This all(Consumer<? super Conjunction> action) {
            return clause(ALL, action);
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
    
    final ClauseType<In<?>> IN = Clauses.IN;
    final ClauseType<IsNull<?>> IS_NULL = Clauses.IS_NULL;
    final ClauseType<Junction> EITHER = Clauses.JUNCTION;
    final ClauseType<Conjunction> ALL = Clauses.CONJUNCTION;
    
    enum Clauses implements ClauseType {
        IS_NULL,
        IN,
        JUNCTION,
        CONJUNCTION;
    }
}
