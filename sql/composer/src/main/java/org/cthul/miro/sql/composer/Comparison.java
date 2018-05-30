package org.cthul.miro.sql.composer;

import java.util.function.BiConsumer;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public interface Comparison {

    Op getOperator();

    Object getValue();
    
    enum Op {
        
        EQUAL("="),
        NOT_EQUAL("!="),
        LESS("<"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        GREATER(">"),
        LIKE("LIKE"),
        ;
        
        private final String op;

        private Op(String op) {
            this.op = op;
        }
    }
    
    static Comparison eq(Object val) {
        return cmp(Op.EQUAL, val);
    }
    
    static Comparison like(Object val) {
        return cmp(Op.LIKE, val);
    }
    
    static Comparison cmp(Op op, Object val) {
        return new Comparison() {
            @Override
            public Op getOperator() {
                return op;
            }
            @Override
            public Object getValue() {
                return val;
            }
            @Override
            public String toString() {
                return getOperator() + " " + getValue();
            }
        };
    }
    
    static boolean isEqual(Object value) {
        if (value instanceof Comparison) {
            return ((Comparison) value).getOperator() == Op.EQUAL;
        }
        return true;
    }
    
    static QlBuilder<?> appendTo(Object value, QlBuilder<?> target) {
//        if (value instanceof Comparison) {
//            Comparison c = (Comparison) value;
//            return target.append(c.getOperator().op).append(" ?")
//                    .pushArgument(c.getValue());
//        } else {
//            return target.append("= ?").pushArgument(value);
//        }
        return appendTo(value, target, (o,q) -> q.append("?").pushArgument(o));
    }
    
    static QlBuilder<?> appendTo(Object value, QlBuilder<?> target, BiConsumer<Object, QlBuilder<?>> writeValue) {
        if (value instanceof Comparison) {
            Comparison c = (Comparison) value;
            target.append(c.getOperator().op).append(" ");
            value = c.getValue();
        } else {
            target.append("= ");
        }
        writeValue.accept(value, target);
        return target;
    }
    
}
