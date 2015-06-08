package org.cthul.miro.result;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.base.BasicEntityType;
import org.cthul.miro.entity.base.EntityPostProcessing;
import org.cthul.miro.test.Address;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class EntityTypesTest {
    
    public EntityTypesTest() {
    }

    @Test
    public void test_multiConfiguration() throws MiException {
        @SuppressWarnings("unchecked")
        EntityConfiguration<Address> cfg = EntityTypes.multiConfiguration(
                new SetStreet("Street 1"), new SetCity("TheCity"));
        Address a = new Address();
        cfg.newInitializer(null).apply(a);
        assertThat(a.street, is("Street 1"));
        assertThat(a.city, is("TheCity"));
    }
    
    static class AddressType extends BasicEntityType<Address> {

        @Override
        protected Address newEntity() {
            return new Address();
        }

        @Override
        public Address[] newArray(int length) {
            return new Address[length];
        }
    }
    
    static class SetStreet extends EntityPostProcessing<Address> {
        private final String street;

        public SetStreet(String street) {
            this.street = street;
        }
        
        @Override
        public void apply(Address entity) throws MiException {
            entity.street = street;
        }
    }
    
    static class SetCity extends EntityPostProcessing<Address> {
        private final String city;

        public SetCity(String city) {
            this.city = city;
        }
        
        @Override
        public void apply(Address entity) throws MiException {
            entity.city = city;
        }
    }
}
