package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.map.MappingKey;

/**
 *
 */
public class PeopleDao2 extends SqlEntitySet<Person, PeopleDao2> {

    public PeopleDao2(MiConnection cnn, MappedSqlType<Person> type) {
        super(cnn, type);
    }

    protected PeopleDao2(SqlEntitySet<Person, PeopleDao2> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        sql("FROM People p");
    }
    
    public PeopleDao2 selectFirstName() {
        return sql(sql -> sql.select().sql("p.first_name AS `firstName`"))
               .setUp(MappingKey.LOAD, "firstName");
    }
    
    public PeopleDao2 withId(int id) {
        return sql(sql -> sql.where().sql("p.id = ?", id));
    }

    public PeopleDao2 selectLastName() {
        return sql(sql -> sql.select().sql("p.last_name AS `lastName`"))
               .setUp(MappingKey.LOAD, "lastName");
    }
    
    public PeopleDao2 selectFirstAndLast() {
        return doSafe(me -> me.selectFirstName().selectLastName());
    }
}
