package org.cthul.miro.lib.msql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.impl.AbstractTypeBuilder;
import org.cthul.miro.result.Results;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.test.Person;
import org.cthul.miro.test.TestDB;
import org.cthul.strings.JavaNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class MaterializationTest {

    @BeforeClass
    public static void setUp() {
        TestDB.clear();
//        insertAddress(1, "Street 1", "City 1");
//        insertAddress(2, "Street 2", "City 2");
        TestDB.insertPerson(1, "John", "Doe", 1);
        TestDB.insertPerson(2, "Jane", "Doe", 2);
//        insertPerson(3, "Bob", "Brown", 1);
//        insertFriend(1, 2);
    }
    
    @AfterClass
    public static void tearDown() {
        TestDB.clear();
    }
    
    private final MiConnection connection = new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax());
    private final EntityType<Person> personType = new SimpleType<>(Person.class);
    
    @Test
    public void test() {
        SelectQuery qry = SelectQuery.create(connection);
        qry.sql("SELECT * FROM People WHERE id = ?", 1);
        
        Person p = qry.submit()
           .andThen(Results.build(personType))
           ._getSingle();
        
        assertThat(p.firstName).is("John");
    }
    
    private static class SimpleType<E> extends AbstractTypeBuilder<E, SimpleType<E>> {

        public SimpleType(Class<E> clazz) {
            super(clazz);
            init();
        }

        public SimpleType(Class<E> clazz, Object shortString) {
            super(clazz, shortString);
            init();
        }
        
        private void init() {
            new TypeAnnotationReader() {
                Map<String, Field> fields = new HashMap<>();
                @Override
                protected void table(String schema, String table, String tableAlias) { }
                @Override
                protected void property(boolean key, Field field) {
                    fields.put(field.getName(), field);
                }
                @Override
                protected void column(String field, String tableAlias, String columnName) {
                    Field f = fields.remove(field);
                    if (columnName == null) columnName = JavaNames.under_score(field);
                    SimpleType.this.optional(columnName).field(f);
                }
            }.read(entityClass());
        }

        @Override
        protected BatchLoader<E> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityFactory<E> newFactory(MiResultSet rs) throws MiException {
            return super.newFactory(rs)
                .with(getAttributes().newInitializer(rs));
        }
    }
}
