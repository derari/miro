package org.cthul.miro.sql.set2;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.db.request.MiUpdateBuilder;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.result.Results;
import org.cthul.miro.set.AbstractImmutable;
import org.cthul.miro.set.DeleteSet;
import org.cthul.miro.set.PropertyFilterSet;
import org.cthul.miro.set.ReadSet;
import org.cthul.miro.set.ValueSet;
import org.cthul.miro.sql.map.MappedSqlType;
import org.cthul.miro.sql.set.Address;
import org.cthul.miro.sql.set.MappedSelectSet;
import org.cthul.miro.sql.set.Person;

public class PeopleDao {
    
    private final FiltersOrValues filters;
    private final Values values;
    private final Read read;
    private final Delete delete;

    public PeopleDao(MiConnection cnn, MappedSqlType<Person> personType) {
        this.filters = new FiltersOrValues();
        this.values = null;
        this.read = new Read(cnn, personType);
        this.delete = null;
    }
    
    public FiltersOrValues getDao() {
        return filters;
    }
    
    public class Filters extends PropertyFilterSet<Filters> {

        public Filters() {
        }

        public Filters(PropertyFilterSet<Filters> source) {
            super(source);
        }

        @Override
        protected Filters copy() {
            return new Filters(this);
        }
        
        public Filters withFirstName(String name) {
            return putFilter("firstName", name);
        }

        public Filters withLastName(String name) {
            return putFilter("lastName", name);
        }

        public Filters withName(String first, String last) {
            return filterProperties("firstName", "lastName").add(first, last);
        }
        
        public Read read() {
            return read.where(this);
        }
        
        public Delete delete() {
            return delete.where(this);
        }

        @Override
        protected void addFiltersTo(PropertyFilter propertyFilter) {
            super.addFiltersTo(propertyFilter);
        }
    }
    
    public class Values extends ValueSet<Person, Values> {
        
        public Read read() {
            return read.where(this);
        }
    }
    
    public class FiltersOrValues extends Filters {
        
        public Values values(Person... people) {
            return values.values(people);
        }
    }
    
    public static class Read extends MappedSelectSet<Person, Read> implements ReadSet<Person> {

        public Read(MiConnection cnn, MappedSqlType<Person> type) {
            super(cnn, type);
        }

        protected Read(Read source) {
            super(source);
        }

        @Override
        protected void initialize() {
            super.initialize();
            setUp(FETCHED_PROPERTIES, "id", "firstName", "lastName");
        }
        
        protected Read where(Filters filters) {
            return withFilterSet(filters);
        }
        
        protected Read where(Values values) {
            return withValues(values);
        }
        
        public Read includeAddress() {
            return setUp(FETCHED_PROPERTIES, "address");
        }

        @Override
        public Results.Action<Person> action() {
            return buildResult();
        }
    }
    
    interface Create {
        
        MiAction<Integer> createAction();
    }
    
//    interface Read {
//        
//        Read includeAddress();
//        
//        MiActionResult<Person> readAction();
//    }
    
    interface Update {
        
        Update setAddress(Address address);
        
        MiAction<Integer> updateAction();
    }
    
    public static class Delete extends AbstractImmutable<Delete> implements DeleteSet {
        
        private final MiConnection cnn;
        private Filters filters;

        public Delete(MiConnection cnn) {
            this.cnn = cnn;
        }

        public Delete(Delete source) {
            super(source);
            this.cnn = source.cnn;
            this.filters = source.filters;
        }

        @Override
        public MiAction<Long> action() {
            MiUpdateBuilder qry = cnn.newUpdate();
            qry.begin(QlBuilder.TYPE)
                    .append("");
            return qry.asAction();
        }

        private Delete where(Filters filters) {
            return doSafe(me -> me.filters = filters);
        }
    }
    
    interface CreateDelegator extends Create {
        
    }
}
