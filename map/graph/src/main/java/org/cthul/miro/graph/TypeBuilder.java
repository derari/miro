package org.cthul.miro.graph;

import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.entity.map.EntityAttributesBuilder;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 * @param <This>
 */
public interface TypeBuilder<Entity, Cnn, This extends TypeBuilder<Entity, Cnn, This>> extends EntityAttributesBuilder<Entity, Cnn, This> {
    
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
    
    static interface Delegator<Entity, Cnn, This extends Delegator<Entity, Cnn, This>> extends TypeBuilder<Entity, Cnn, This> {
        
        TypeBuilder<Entity, Cnn, ?> internalTypeBuilder();

        @Override
        default This key(String attribute) {
            internalTypeBuilder().key(attribute);
            return (This) this;
        }

        @Override
        default This constructor(Function<Object[], Entity> constructor) {
            internalTypeBuilder().constructor(constructor);
            return (This) this;
        }

        @Override
        default This constructor(Supplier<Entity> constructor) {
            internalTypeBuilder().constructor(constructor);
            return (This) this;
        }
    }
}
