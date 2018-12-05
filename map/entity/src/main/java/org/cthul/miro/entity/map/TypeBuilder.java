package org.cthul.miro.entity.map;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface TypeBuilder<Entity, This extends TypeBuilder<Entity, This>> extends EntityPropertiesBuilder<Entity, This> {
    
    This key(String attribute);
    
    default This keys(String... attributes) {
        This me = (This) this;
        for (String a: attributes) {
            me = key(a);
        }
        return me;
    }
    
    default This keys(Iterable<String> attributes) {
        This me = (This) this;
        for (String a: attributes) {
            me = key(a);
        }
        return me;
    }
    
    This constructor(Supplier<Entity> constructor);
    
    This constructor(Function<Object[], Entity> constructor);
    
//    static interface Delegator<Entity, This extends Delegator<Entity, This>> extends TypeBuilder<Entity, This> {
//        
//        TypeBuilder<Entity, ?> internalTypeBuilder();
//
//        @Override
//        default This key(String attribute) {
//            internalTypeBuilder().key(attribute);
//            return (This) this;
//        }
//
//        @Override
//        default This constructor(Function<Object[], Entity> constructor) {
//            internalTypeBuilder().constructor(constructor);
//            return (This) this;
//        }
//
//        @Override
//        default This constructor(Supplier<Entity> constructor) {
//            internalTypeBuilder().constructor(constructor);
//            return (This) this;
//        }
//    }
}
