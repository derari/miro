package org.cthul.miro.result;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.test.Address;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EntityTypesTest {
    
    public EntityTypesTest() {
    }

    @Test
    public void test_multiConfiguration() throws MiException {
        @SuppressWarnings("unchecked")
        EntityConfiguration<Address> cfg = (rs,b) -> b.add(new SetStreet("Street 1")).add(new SetCity("TheCity"));
        Address a = new Address();
        cfg.newInitializer(null).apply(a);
        assertThat(a.street, is("Street 1"));
        assertThat(a.city, is("TheCity"));
    }
    
//    static class AddressType extends BasicEntityType<Address> {
//
//        @Override
//        protected Address newEntity() {
//            return new Address();
//        }
//    }
    
    static class SetStreet implements EntityInitializer<Address> {
        private final String street;

        public SetStreet(String street) {
            this.street = street;
        }
        
        @Override
        public void apply(Address entity) throws MiException {
            entity.street = street;
        }

        @Override
        public String toString() {
            return "street = " + street;
        }
    }
    
    static class SetCity implements EntityInitializer<Address> {
        private final String city;

        public SetCity(String city) {
            this.city = city;
        }
        
        @Override
        public void apply(Address entity) throws MiException {
            entity.city = city;
        }

        @Override
        public String toString() {
            return "city = " + city;
        }
    }
}
